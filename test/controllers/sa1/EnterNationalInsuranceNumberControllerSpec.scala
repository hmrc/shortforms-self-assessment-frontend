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
import controllers.MockAppControllerComponents
import forms.sa1.EnterNationalInsuranceNumberFormProvider
import models.{JourneyType, NormalMode, UserAnswers}
import navigation.FakeNavigator
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.sa1.{DoYouHaveANinoPage, EnterNationalInsuranceNumberPage}
import play.api.data.Form
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import play.twirl.api.Html
import repositories.SessionRepository
import views.html.sa1.EnterNationalInsuranceNumberView

import scala.concurrent.Future

class EnterNationalInsuranceNumberControllerSpec extends SpecBase with MockitoSugar {
  private val journeyId = "test-journey-id"
  private val userId    = "test-user-id"

  private val doYouHaveANinoRoute = routes.DoYouHaveANinoController.onPageLoad(journeyId, NormalMode)
  def onwardRoute                 = Call("GET", "/foo")

  private val enterNationalInsuranceNumber = "NM772208B"
  private val validFormData                = Map("value" -> enterNationalInsuranceNumber)
  protected val mockViewContent            = "EnterNationalInsuranceNumberView"

  private trait Setup {
    protected def userAnswers: UserAnswers =
      UserAnswers(journeyId, userId, JourneyType.SA1).update(DoYouHaveANinoPage, true)

    protected val sessionRepository: SessionRepository   = mock
    protected val view: EnterNationalInsuranceNumberView = mock
    when(view.apply(any[Form[_]], any[String], any)(any, any)) thenReturn Html(mockViewContent)

    protected lazy val controllerUnderTest = new EnterNationalInsuranceNumberController(
      new MockAppControllerComponents(userAnswers),
      sessionRepository,
      new EnterNationalInsuranceNumberFormProvider(),
      new FakeNavigator(onwardRoute),
      view
    )
  }

  "page load handler should" - {

    "must return OK and the correct view for a GET" in new Setup {
      private val result = controllerUnderTest.onPageLoad(journeyId, NormalMode)(FakeRequest())

      status(result) mustEqual OK
      contentAsString(result) mustEqual mockViewContent
    }

    "pre-populate the form when previous contact details exists " in new Setup {
      override val userAnswers: UserAnswers =
        super.userAnswers.update(EnterNationalInsuranceNumberPage, enterNationalInsuranceNumber)

      private val result = controllerUnderTest.onPageLoad(journeyId, NormalMode)(FakeRequest())
      status(result) mustEqual OK

      private val captor = ArgumentCaptor.forClass(classOf[Form[String]])
      verify(view).apply(captor.capture(), eqTo(journeyId), any)(any, any)

      private val populatedForm = captor.getValue
      populatedForm("value").value mustBe Some(enterNationalInsuranceNumber)
    }

    "redirect to 'Do You Have a Nino' page WHEN" - {
      "question was answered 'No'" in new Setup {
        override val userAnswers: UserAnswers = emptyUserAnswers.update(DoYouHaveANinoPage, false)

        private val result = controllerUnderTest.onPageLoad(journeyId, NormalMode)(FakeRequest())

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual doYouHaveANinoRoute.url
      }

      "question was NOT answered at all" in new Setup {
        override val userAnswers: UserAnswers = emptyUserAnswers.delete(DoYouHaveANinoPage)

        private val result = controllerUnderTest.onPageLoad(journeyId, NormalMode)(FakeRequest())

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual doYouHaveANinoRoute.url
      }
    }
  }

  "submission handler should" - {

    "redirect to 'Do You Have a Nino' page WHEN" - {
      "question was answered 'No'" in new Setup {
        override val userAnswers: UserAnswers = emptyUserAnswers.update(DoYouHaveANinoPage, false)

        private val result = controllerUnderTest.onSubmit(journeyId, NormalMode)(FakeRequest())

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual doYouHaveANinoRoute.url
      }

      "question was NOT answered at all" in new Setup {
        override val userAnswers: UserAnswers = emptyUserAnswers.delete(DoYouHaveANinoPage)

        private val result = controllerUnderTest.onSubmit(journeyId, NormalMode)(FakeRequest())

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual doYouHaveANinoRoute.url
      }
    }

    "return BAD_REQUEST when the submitted form is invalid" in new Setup {
      private val request = FakeRequest().withFormUrlEncodedBody("value" -> "")
      private val result  = controllerUnderTest.onSubmit(journeyId, NormalMode)(request)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe mockViewContent
    }

    "save the submitted data and redirect when the form is valid" in new Setup {
      when(sessionRepository.set(any[UserAnswers])).thenReturn(Future.successful(true))

      private val request = FakeRequest().withFormUrlEncodedBody(validFormData.toSeq*)
      private val result  = controllerUnderTest.onSubmit(journeyId, NormalMode)(request)

      status(result) mustBe SEE_OTHER
      redirectLocation(result).value mustBe onwardRoute.url

      private val captor = ArgumentCaptor.forClass(classOf[UserAnswers])
      verify(sessionRepository).set(captor.capture())
      captor.getValue.get(EnterNationalInsuranceNumberPage) mustBe Some(enterNationalInsuranceNumber)
    }
  }
}
