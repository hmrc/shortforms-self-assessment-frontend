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

import controllers.MockAppControllerComponents
import controllers.sa1.EnterNationalInsuranceNumberController
import forms.sa1.EnterNationalInsuranceNumberFormProvider
import models.{JourneyType, NormalMode, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{verify, when}
import org.scalatest.TryValues
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import pages.sa1.EnterNationalInsuranceNumberPage
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.{AnyContentAsFormUrlEncoded, Call, Request, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import play.twirl.api.Html
import repositories.SessionRepository
import views.html.sa1.EnterNationalInsuranceNumberView

import scala.concurrent.{ExecutionContext, Future}

class EnterNationalInsuranceNumberControllerSpec extends AnyWordSpec with Matchers with TryValues with MockitoSugar {

  private implicit val ec: ExecutionContext = ExecutionContext.global

  private val journeyId = "test-journey-id"
  private val userId    = "test-user-id"

  def onwardRoute = Call("GET", "/foo")

  private val enterNationalInsuranceNumber = "NM772208B"
  private val validFormData                = Map("value" -> enterNationalInsuranceNumber)
  protected val mockViewContent            = "EnterNationalInsuranceNumberView"

  lazy val enterNationalInsuranceNumberRoute: String =
    controllers.sa1.routes.EnterNationalInsuranceNumberController.onPageLoad("test-journey-id", NormalMode).url

  private trait Setup {
    protected def userAnswers: UserAnswers = UserAnswers(journeyId, userId, JourneyType.SA1)

    protected val sessionRepository: SessionRepository   = mock[SessionRepository]
    protected val view: EnterNationalInsuranceNumberView = mock[EnterNationalInsuranceNumberView]

    protected lazy val controllerUnderTest = new EnterNationalInsuranceNumberController(
      new MockAppControllerComponents(userAnswers),
      sessionRepository,
      new EnterNationalInsuranceNumberFormProvider(),
      view
    )
    when(view.apply(any[Form[_]], any[String], any)(any[Request[?]], any[Messages])).thenReturn(Html(mockViewContent))

  }

  "EnterNationalInsuranceNumber Controller" should {

    "must return OK and the correct view for a GET" in new Setup {

      private val result = controllerUnderTest.onPageLoad(journeyId, NormalMode)(FakeRequest())

      status(result) mustEqual OK
      contentAsString(result) mustEqual mockViewContent
    }

    "pre-populate the form when previous contact details exists " in new Setup {

      override protected val userAnswers: UserAnswers = UserAnswers(journeyId, userId, JourneyType.SA1)
        .set(EnterNationalInsuranceNumberPage, enterNationalInsuranceNumber)
        .success
        .value
      private val result                              = controllerUnderTest.onPageLoad(journeyId, NormalMode)(FakeRequest())
      status(result) mustEqual OK
      val captor: ArgumentCaptor[Form[String]]        = ArgumentCaptor.forClass(classOf[Form[String]])
      verify(view).apply(captor.capture(), eqTo(journeyId), any)(any[Request[?]], any[Messages])

      val populatedForm: Form[String] = captor.getValue
      populatedForm("value").value mustBe Some(enterNationalInsuranceNumber)
    }

    "return BAD_REQUEST when the submitted form is invalid" in new Setup {
      val request: FakeRequest[AnyContentAsFormUrlEncoded] = FakeRequest(POST, "/").withFormUrlEncodedBody(
        "value" -> ""
      )

      val result: Future[Result] = controllerUnderTest.onSubmit(journeyId, NormalMode)(request)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe mockViewContent
    }

    "save the submitted data and redirect when the form is valid" in new Setup {
      when(sessionRepository.set(any[UserAnswers])).thenReturn(Future.successful(true))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] =
        FakeRequest(POST, "/").withFormUrlEncodedBody(validFormData.toSeq*)

      val result: Future[Result] = controllerUnderTest.onSubmit(journeyId, NormalMode)(request)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(
        controllers.sa1.routes.EnterNationalInsuranceNumberController.onPageLoad(journeyId, NormalMode).url
      ) // TODO: Change URL post define right reroute URL

      val captor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
      verify(sessionRepository).set(captor.capture())
      captor.getValue.get(EnterNationalInsuranceNumberPage) mustBe Some(enterNationalInsuranceNumber)
    }
  }
}
