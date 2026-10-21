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

package controllers

import base.SpecBase
import config.AddressLookupConfiguration
import connectors.AddressLookupConnector
import models.{JourneyType, NormalMode, UserAnswers}
import models.address.{AddressDetails, ConfirmedAddress, Country}
import org.mockito.{ArgumentCaptor, ArgumentMatchers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{mock, verify, verifyNoInteractions, when}
import pages.AddressPage
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{Call, RequestHeader, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class AddressLookupControllerSpec extends SpecBase {

  JourneyType.values.foreach { journeyType =>
    s"AddressLookupController for $journeyType" - {

      Seq(false, true).foreach { changing =>
        val actionName = if (changing) "change" else "start"
        s"$actionName must initialise ALF with the correct generic callback" in new SetupWithSavedAddress(journeyType) {
          val result   =
            if (changing) controller.change(userAnswersId)(FakeRequest(GET, "/"))
            else controller.start(userAnswersId)(FakeRequest(GET, "/"))
          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe alfUrl
          val callback =
            if (changing) routes.AddressLookupController.callbackChange(userAnswersId)
            else routes.AddressLookupController.callback(userAnswersId)
          verify(mockConfiguration).apply(ArgumentMatchers.eq(callback))(any[RequestHeader])
          verify(mockConnector).initialise(ArgumentMatchers.eq(configuration))(any[HeaderCarrier])
          verifyNoInteractions(mockSessionRepository)
        }

        s"$actionName must recover when ALF initialisation fails" in new Setup(journeyType) {
          when(mockConnector.initialise(any[JsObject])(any[HeaderCarrier]))
            .thenReturn(Future.failed(new RuntimeException("ALF unavailable")))
          val result =
            if (changing) controller.change(userAnswersId)(FakeRequest(GET, "/"))
            else controller.start(userAnswersId)(FakeRequest(GET, "/"))
          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe recoveryUrl
        }
      }

      Seq(false, true).foreach { changing =>
        val callbackName = if (changing) "callbackChange" else "callback"

        callbackName - {
          "must save the full address and UPRN before continuing to the appropriate page" in new Setup(journeyType) {
            val result = confirmAddress(changing)

            status(result) mustBe SEE_OTHER
            val destination = if (!changing && journeyType == JourneyType.SA1) {
              controllers.sa1.routes.AddPreviousLastNameController.onPageLoad(userAnswersId, NormalMode)
            } else controllers.sa1.routes.CheckYourAnswersController.onPageLoad(userAnswersId)
            redirectLocation(result).value mustBe destination.url
            verify(mockConnector).getAddress(ArgumentMatchers.eq("alf-id"))(any[HeaderCarrier])
            val saved       = capturedAnswers()
            saved.get(AddressPage).value mustBe confirmed
            saved.id mustBe userAnswersId
            saved.userId mustBe testUserId
            saved.journeyType mustBe journeyType
          }

          "must replace an existing address with a manual international address without a UPRN" in new SetupWithSavedAddress(
            journeyType
          ) {
            val manual = ConfirmedAddress(
              AddressDetails(Seq("1 Rue Exemple"), town = Some("Paris"), country = Some(Country("FR", "France"))),
              auditRef = "manual-audit"
            )
            when(mockConnector.getAddress(any[String])(any[HeaderCarrier])).thenReturn(Future.successful(manual))

            val result = confirmAddress(changing)

            status(result) mustBe SEE_OTHER
            capturedAnswers().get(AddressPage).value mustBe manual
          }

          Seq("", " ").foreach { missingId =>
            s"must recover without calling ALF or saving when the ID is '$missingId'" in new Setup(journeyType) {
              val result = confirmAddress(changing, missingId)

              status(result) mustBe SEE_OTHER
              redirectLocation(result).value mustBe recoveryUrl
              verifyNoInteractions(mockConnector, mockSessionRepository)
            }
          }

          "must recover without saving when address retrieval fails" in new Setup(journeyType) {
            when(mockConnector.getAddress(any[String])(any[HeaderCarrier]))
              .thenReturn(Future.failed(new RuntimeException("ALF unavailable")))

            val result = confirmAddress(changing)

            status(result) mustBe SEE_OTHER
            redirectLocation(result).value mustBe recoveryUrl
            verifyNoInteractions(mockSessionRepository)
          }

          "must recover when saving fails" in new Setup(journeyType) {
            when(mockSessionRepository.set(any[UserAnswers]))
              .thenReturn(Future.failed(new RuntimeException("Mongo unavailable")))

            val result = confirmAddress(changing)

            status(result) mustBe SEE_OTHER
            redirectLocation(result).value mustBe recoveryUrl
          }

          "must recover when the repository reports that the address was not saved" in new Setup(journeyType) {
            when(mockSessionRepository.set(any[UserAnswers])).thenReturn(Future.successful(false))

            val result = confirmAddress(changing)

            status(result) mustBe SEE_OTHER
            redirectLocation(result).value mustBe recoveryUrl
          }
        }
      }
    }
  }

  abstract class Setup(val formType: JourneyType) {
    def userAnswers: UserAnswers = emptyUserAnswers.copy(journeyType = formType)

    val alfUrl                      = "http://localhost:9028/lookup-address/test-alf-journey"
    val recoveryUrl: String         = routes.JourneyRecoveryController.onPageLoad().url
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

    when(mockConfiguration.apply(any[Call])(any[RequestHeader])).thenReturn(configuration)
    when(mockConnector.initialise(any[JsObject])(any[HeaderCarrier])).thenReturn(Future.successful(alfUrl))
    when(mockConnector.getAddress(any[String])(any[HeaderCarrier])).thenReturn(Future.successful(confirmed))
    when(mockSessionRepository.set(any[UserAnswers])).thenReturn(Future.successful(true))

    lazy val controller = new AddressLookupController(
      new MockAppControllerComponents(userAnswers),
      mockConnector,
      mockConfiguration,
      mockSessionRepository
    )

    def confirmAddress(changing: Boolean, id: String = "alf-id"): Future[Result] =
      if (changing) {
        controller.callbackChange(userAnswersId, id)(FakeRequest(GET, "/"))
      } else {
        controller.callback(userAnswersId, id)(FakeRequest(GET, "/"))
      }

    def capturedAnswers(): UserAnswers = {
      val captor = ArgumentCaptor.forClass(classOf[UserAnswers])
      verify(mockSessionRepository).set(captor.capture())
      captor.getValue
    }
  }

  abstract class SetupWithSavedAddress(journeyType: JourneyType) extends Setup(journeyType) {
    override def userAnswers: UserAnswers =
      emptyUserAnswers.copy(journeyType = formType).set(AddressPage, confirmed).success.value
  }
}
