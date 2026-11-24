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
import controllers.MockAppControllerComponents
import forms.EnterNiNumberFormProvider
import models.JourneyType.CWF1ST
import models.{JourneyType, NormalMode, UserAnswers}
import navigation.Cwf1Navigator
import org.scalatestplus.mockito.MockitoSugar
import pages.EnterNiNumberPage
import play.api.data.Form
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.cwf1.EnterNiNumberView

import scala.concurrent.Future

class EnterNiNumberControllerSpec extends SpecBase with MockitoSugar {
  import navigation.FakeNavigator
  import org.mockito.ArgumentCaptor
  import org.mockito.ArgumentMatchers.{any, eq as eqTo}
  import org.mockito.Mockito.{verify, when}

  private val journeyId = "test-journey-id"
  private val userId    = "test-user-id"

  private val onwardRoute = Call("GET", "/next-page")

  private val givenNiNumber = "NM772208B"
  private val validFormData = Map("value" -> givenNiNumber)

  private trait Setup {
    protected def userAnswers: UserAnswers = UserAnswers(journeyId, userId, JourneyType.CWF1ST)

    protected val sessionRepository: SessionRepository = mock
    protected val view: EnterNiNumberView              = mock
    protected val mockViewContent                      = "EnterNationalInsuranceNumber"
    when(view.apply(any[Form[_]], any[String], any)(any, any)) thenReturn play.twirl.api.Html(mockViewContent)

    protected lazy val controllerUnderTest = new EnterNiNumberController(
      new MockAppControllerComponents(userAnswers),
      sessionRepository,
      new EnterNiNumberFormProvider(),
      new Cwf1Navigator with FakeNavigator(onwardRoute),
      view
    )
  }

  "EnterNationalInsuranceNumber Controller must" - {

    "return OK and the correct view for a GET" in new Setup {
      private val result = controllerUnderTest.onPageLoad(journeyId, NormalMode)(FakeRequest())

      status(result) mustEqual OK
      contentAsString(result) mustEqual mockViewContent
    }

    "pre-populate the form when NI number previous submitted" in new Setup {
      override val userAnswers: UserAnswers = UserAnswers(journeyId, userId, CWF1ST)
        .set(EnterNiNumberPage, givenNiNumber)
        .success
        .value

      private val result = controllerUnderTest.onPageLoad(journeyId, NormalMode)(FakeRequest())

      status(result) mustEqual OK

      private val captor = ArgumentCaptor forClass classOf[Form[String]]
      verify(view).apply(captor.capture(), eqTo(journeyId), any)(any, any)

      private val populatedForm = captor.getValue
      populatedForm("value").value mustBe Some(givenNiNumber)
    }

    "return BAD_REQUEST when" - {
      "when NI number is invalid" in new Setup {
        private val request = FakeRequest().withFormUrlEncodedBody("value" -> "")

        private val result = controllerUnderTest.onSubmit(journeyId, NormalMode)(request)

        status(result) mustBe BAD_REQUEST
        contentAsString(result) mustBe mockViewContent
      }

      "when NI number doesn't match the one retrieved from GG" in new Setup {
        private val request = FakeRequest().withFormUrlEncodedBody("value" -> "MN772208B")

        private val result = controllerUnderTest.onSubmit(journeyId, NormalMode)(request)

        status(result) mustBe BAD_REQUEST
        contentAsString(result) mustBe mockViewContent
      }
    }

    "save the submitted data and redirect when the form is valid" in new Setup {
      when(sessionRepository.set(any[UserAnswers])) thenReturn Future.successful(true)

      private val request = FakeRequest().withFormUrlEncodedBody(validFormData.toSeq*)

      private val result = controllerUnderTest.onSubmit(journeyId, NormalMode)(request)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)

      private val captor = ArgumentCaptor forClass classOf[UserAnswers]
      verify(sessionRepository).set(captor.capture())
      captor.getValue.get(EnterNiNumberPage) mustBe Some(givenNiNumber)
    }
  }
}
