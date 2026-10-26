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

package controllers.sa1

import base.SpecBase
import config.AddressLookupConfiguration
import connectors.AddressLookupConnector
import controllers.MockAppControllerComponents
import models.{NormalMode, UserAnswers}
import models.address.{AddressDetails, ConfirmedAddress, Country}
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{mock, verify, verifyNoInteractions, when}
import org.mockito.ArgumentCaptor
import pages.AddressPage
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{Call, RequestHeader, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class AddressLookupControllerSpec extends SpecBase {

  "SA1 AddressLookupController" - {
    Seq(false, true).foreach { changing =>
      val actionName = if (changing) "change" else "start"

      s"$actionName must initialise ALF with the correct callback and SA1 landing page" in new SetupWithSavedAddress {
        val result = startJourney(changing)

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe alfUrl
        val callback =
          if (changing) routes.AddressLookupController.callbackChange(userAnswersId)
          else routes.AddressLookupController.callback(userAnswersId)
        verify(mockConfiguration).apply(eqTo(callback), eqTo(landingPage))(any[RequestHeader])
        verify(mockConnector).initialise(eqTo(configuration))(any[HeaderCarrier])
        verifyNoInteractions(mockSessionRepository)
      }

      s"$actionName must recover when ALF initialisation fails" in new Setup {
        when(mockConnector.initialise(any[JsObject])(any[HeaderCarrier]))
          .thenReturn(Future.failed(new RuntimeException("ALF unavailable")))

        val result = startJourney(changing)

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe recoveryUrl
      }
    }

    Seq(false, true).foreach { changing =>
      val callbackName = if (changing) "callbackChange" else "callback"

      callbackName - {
        "must save the full address and UPRN before continuing" in new Setup {
          val result = confirmAddress(changing)

          status(result) mustBe SEE_OTHER
          val destination =
            if (changing) routes.CheckYourAnswersController.onPageLoad(userAnswersId)
            else routes.AddPreviousLastNameController.onPageLoad(userAnswersId, NormalMode)
          redirectLocation(result).value mustBe destination.url
          verify(mockConnector).getAddress(eqTo("alf-id"))(any[HeaderCarrier])
          val saved       = capturedAnswers()
          saved.get(AddressPage).value mustBe confirmed
          saved.id mustBe userAnswersId
          saved.userId mustBe testUserId
        }

        "must replace an existing address with a manual international address" in new SetupWithSavedAddress {
          val manual = ConfirmedAddress(
            AddressDetails(Seq("1 Rue Exemple"), town = Some("Paris"), country = Some(Country("FR", "France"))),
            auditRef = "manual-audit"
          )
          when(mockConnector.getAddress(any[String])(any[HeaderCarrier])).thenReturn(Future.successful(manual))

          status(confirmAddress(changing)) mustBe SEE_OTHER
          capturedAnswers().get(AddressPage).value mustBe manual
        }

        Seq("", " ").foreach { missingId =>
          s"must recover without calling ALF or saving when the ID is '$missingId'" in new Setup {
            val result = confirmAddress(changing, missingId)

            status(result) mustBe SEE_OTHER
            redirectLocation(result).value mustBe recoveryUrl
            verifyNoInteractions(mockConnector, mockSessionRepository)
          }
        }

        "must recover without saving when address retrieval fails" in new Setup {
          when(mockConnector.getAddress(any[String])(any[HeaderCarrier]))
            .thenReturn(Future.failed(new RuntimeException("ALF unavailable")))

          status(confirmAddress(changing)) mustBe SEE_OTHER
          verifyNoInteractions(mockSessionRepository)
        }

        "must recover when saving fails" in new Setup {
          when(mockSessionRepository.set(any[UserAnswers]))
            .thenReturn(Future.failed(new RuntimeException("Mongo unavailable")))

          status(confirmAddress(changing)) mustBe SEE_OTHER
          redirectLocation(confirmAddress(changing)).value mustBe recoveryUrl
        }
      }
    }
  }

  trait Setup {
    def userAnswers: UserAnswers = emptyUserAnswers

    val alfUrl: String              = "http://localhost:9028/lookup-address/test-alf-journey"
    val recoveryUrl: String         = controllers.routes.JourneyRecoveryController.onPageLoad().url
    val landingPage: Call           = routes.StartController.onPageLoad()
    val configuration: JsObject     = Json.obj("version" -> 2)
    val confirmed: ConfirmedAddress = ConfirmedAddress(
      AddressDetails(
        Seq("An address line longer than twenty seven characters", "District"),
        town = Some("Town"),
        postcode = Some("AA1 1AA"),
        country = Some(Country("GB", "United Kingdom"))
      ),
      auditRef = "audit-reference",
      id = Some("GB-address-id"),
      uprn = Some(123456789L)
    )

    val mockConfiguration: AddressLookupConfiguration = mock(classOf[AddressLookupConfiguration])
    val mockConnector: AddressLookupConnector         = mock(classOf[AddressLookupConnector])
    val mockSessionRepository: SessionRepository      = mock(classOf[SessionRepository])

    when(mockConfiguration.apply(any[Call], any[Call])(any[RequestHeader])).thenReturn(configuration)
    when(mockConnector.initialise(any[JsObject])(any[HeaderCarrier])).thenReturn(Future.successful(alfUrl))
    when(mockConnector.getAddress(any[String])(any[HeaderCarrier])).thenReturn(Future.successful(confirmed))
    when(mockSessionRepository.set(any[UserAnswers])).thenReturn(Future.successful(true))

    lazy val controller = new AddressLookupController(
      new MockAppControllerComponents(userAnswers),
      mockConnector,
      mockConfiguration,
      mockSessionRepository
    )

    def startJourney(changing: Boolean): Future[Result] =
      if (changing) controller.change(userAnswersId)(FakeRequest(GET, "/"))
      else controller.start(userAnswersId)(FakeRequest(GET, "/"))

    def confirmAddress(changing: Boolean, id: String = "alf-id"): Future[Result] =
      if (changing) controller.callbackChange(userAnswersId, id)(FakeRequest(GET, "/"))
      else controller.callback(userAnswersId, id)(FakeRequest(GET, "/"))

    def capturedAnswers(): UserAnswers = {
      val captor = ArgumentCaptor.forClass(classOf[UserAnswers])
      verify(mockSessionRepository).set(captor.capture())
      captor.getValue
    }
  }

  trait SetupWithSavedAddress extends Setup {
    override def userAnswers: UserAnswers = emptyUserAnswers.set(AddressPage, confirmed).success.value
  }
}
