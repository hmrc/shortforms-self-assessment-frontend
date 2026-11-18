/*
 * Copyright 2026 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers.cwf1

import base.SpecBase
import config.AddressLookupConfiguration
import connectors.AddressLookupConnector
import controllers.MockAppControllerComponents
import models.{CheckMode, JourneyType, Mode, NormalMode, UserAnswers}
import models.cwf1.AddressType
import models.address.{AddressDetails, ConfirmedAddress, Country}
import navigation.Navigator
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{mock, verify, verifyNoInteractions, when}
import pages.Page
import pages.cwf1.{AgentAddressPage, HomeAddressPage, PreviousHomeAddressPage}
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{Call, RequestHeader, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier

import ch.qos.logback.classic.Level
import play.api.Logger
import uk.gov.hmrc.play.bootstrap.tools.LogCapturing

import scala.concurrent.Future

class AddressLookupControllerSpec extends SpecBase with LogCapturing {

  private def mustFailAndLogError(message: String)(result: => Future[Result]): Throwable = {
    var failure: Throwable = null
    withCaptureOfLoggingFrom(Logger(classOf[AddressLookupController])) { logs =>
      failure = result.failed.futureValue
      logs.filter(_.getLevel == Level.ERROR).map(_.getMessage) mustBe List(message)
    }
    failure
  }

  private val addressTypes = Seq(
    (AddressType.Agent, AgentAddressPage, "cwf1.agentAddress", "agent"),
    (AddressType.Home, HomeAddressPage, "cwf1.homeAddress", "home"),
    (AddressType.PreviousHome, PreviousHomeAddressPage, "cwf1.previousHomeAddress", "previous-home")
  )

  "AddressLookupController" - {
    addressTypes.foreach { (addressType, page, messagePrefix, segment) =>
      s"for the $addressType address" - {
        Seq(false, true).foreach { changing =>
          val actionName = if (changing) "change" else "start"

          s"$actionName must initialise ALF with the $addressType callback, wording and CWF1 landing page" in new Setup {
            val result = startJourney(addressType, changing)

            status(result) mustBe SEE_OTHER
            redirectLocation(result).value mustBe alfUrl
            val callback =
              if (changing) routes.AddressLookupController.callbackChange(addressType, userAnswersId)
              else routes.AddressLookupController.callback(addressType, userAnswersId)
            callback.url must include(s"/$segment/")
            verify(mockConfiguration).apply(
              eqTo(callback),
              eqTo(routes.StartController.onPageLoad()),
              eqTo(messagePrefix),
              eqTo("service.cwf1st.name")
            )(any[RequestHeader])
            verify(mockConnector).initialise(eqTo(configuration))(any[HeaderCarrier])
            verifyNoInteractions(mockSessionRepository, mockNavigator)
          }

          s"$actionName must log and fail when ALF initialisation fails" in new Setup {
            when(mockConnector.initialise(any[JsObject])(any[HeaderCarrier]))
              .thenReturn(Future.failed(new RuntimeException("ALF unavailable")))

            mustFailAndLogError(s"Failed to initialise the CWF1 $addressType address lookup journey")(
              startJourney(addressType, changing)
            ).getMessage mustBe "ALF unavailable"
          }

          val callbackName = if (changing) "callbackChange" else "callback"
          val mode: Mode   = if (changing) CheckMode else NormalMode

          s"$callbackName must save only the $addressType address and continue in $mode" in new Setup {
            val result = confirmAddress(addressType, changing)

            status(result) mustBe SEE_OTHER
            redirectLocation(result).value mustBe nextPage.url
            verify(mockConnector).getAddress(eqTo("alf-id"))(any[HeaderCarrier])
            val saved = capturedAnswers()
            saved.get(page).value mustBe confirmed
            addressTypes.map(_._2).filterNot(_ == page).foreach(other => saved.get(other) mustBe None)
            verify(mockNavigator).nextPage(eqTo(page), eqTo(mode), eqTo(saved))
          }

          Seq("", " ").foreach { missingId =>
            s"$callbackName must log and fail without calling ALF or saving when the ID is '$missingId'" in new Setup {
              mustFailAndLogError(s"Failed to retrieve or save the confirmed CWF1 $addressType address")(
                confirmAddress(addressType, changing, missingId)
              ) mustBe an[IllegalArgumentException]
              verifyNoInteractions(mockConnector, mockSessionRepository)
            }
          }

          s"$callbackName must log and fail without continuing when saving fails" in new Setup {
            when(mockSessionRepository.set(any[UserAnswers]))
              .thenReturn(Future.failed(new RuntimeException("Mongo unavailable")))

            mustFailAndLogError(s"Failed to retrieve or save the confirmed CWF1 $addressType address")(
              confirmAddress(addressType, changing)
            ).getMessage mustBe "Mongo unavailable"
            verifyNoInteractions(mockNavigator)
          }
        }
      }
    }
  }

  trait Setup {
    val alfUrl: String              = "http://localhost:9028/lookup-address/test-alf-journey"
    val nextPage: Call              = Call(GET, "/next-page")
    val configuration: JsObject     = Json.obj("version" -> 2)
    val confirmed: ConfirmedAddress = ConfirmedAddress(
      AddressDetails(
        Seq("1 Test Street"),
        town = Some("Town"),
        postcode = Some("AA1 1AA"),
        country = Some(Country("GB", "United Kingdom"))
      ),
      auditRef = "audit-reference",
      id = Some("GB-address-id")
    )

    val mockConfiguration: AddressLookupConfiguration = mock(classOf[AddressLookupConfiguration])
    val mockConnector: AddressLookupConnector         = mock(classOf[AddressLookupConnector])
    val mockSessionRepository: SessionRepository      = mock(classOf[SessionRepository])
    val mockNavigator: Navigator                      = mock(classOf[Navigator])

    when(mockConfiguration.apply(any[Call], any[Call], any[String], any[String])(any[RequestHeader]))
      .thenReturn(configuration)
    when(mockConnector.initialise(any[JsObject])(any[HeaderCarrier])).thenReturn(Future.successful(alfUrl))
    when(mockConnector.getAddress(any[String])(any[HeaderCarrier])).thenReturn(Future.successful(confirmed))
    when(mockSessionRepository.set(any[UserAnswers])).thenReturn(Future.successful(true))
    when(mockNavigator.nextPage(any[Page], any[Mode], any[UserAnswers])).thenReturn(nextPage)

    lazy val controller = new AddressLookupController(
      new MockAppControllerComponents(UserAnswers(userAnswersId, testUserId, JourneyType.CWF1ST)),
      mockConnector,
      mockConfiguration,
      mockSessionRepository,
      mockNavigator
    )

    def startJourney(addressType: AddressType, changing: Boolean): Future[Result] =
      if (changing) controller.change(addressType, userAnswersId)(FakeRequest(GET, "/"))
      else controller.start(addressType, userAnswersId)(FakeRequest(GET, "/"))

    def confirmAddress(addressType: AddressType, changing: Boolean, id: String = "alf-id"): Future[Result] =
      if (changing) controller.callbackChange(addressType, userAnswersId, id)(FakeRequest(GET, "/"))
      else controller.callback(addressType, userAnswersId, id)(FakeRequest(GET, "/"))

    def capturedAnswers(): UserAnswers = {
      val captor = ArgumentCaptor.forClass(classOf[UserAnswers])
      verify(mockSessionRepository).set(captor.capture())
      captor.getValue
    }
  }
}
