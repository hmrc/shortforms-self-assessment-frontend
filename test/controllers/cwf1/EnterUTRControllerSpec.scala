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
import forms.cwf1.EnterUTRFormProvider
import models.{NormalMode, UserAnswers}
import navigation.{Cwf1Navigator, FakeNavigator}
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.cwf1.EnterUTRPage
import play.api.data.Form
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import play.twirl.api.Html
import repositories.SessionRepository
import views.html.cwf1.EnterUTRView

import scala.concurrent.Future

class EnterUTRControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  "EnterUTR Controller" - {

    "must return OK and the correct view for a GET" in new Setup {
      val userAnswers: UserAnswers = emptyUserAnswers

      private val result = controllerUnderTest.onPageLoad(userAnswersId, NormalMode)(FakeRequest())

      status(result) mustEqual OK
      contentAsString(result) mustEqual mockContent

      verify(mockView).apply(
        eqTo(form),
        eqTo(userAnswersId),
        eqTo(NormalMode)
      )(any(), any())
    }

    "must populate the view correctly on a GET when the question has previously been answered" in new Setup {
      val userAnswers: UserAnswers =
        emptyUserAnswers.set(EnterUTRPage, "1234567890").success.value

      private val result = controllerUnderTest.onPageLoad(userAnswersId, NormalMode)(FakeRequest())

      status(result) mustEqual OK
      contentAsString(result) mustEqual mockContent

      verify(mockView).apply(
        eqTo(form.fill("1234567890")),
        eqTo(userAnswersId),
        eqTo(NormalMode)
      )(any(), any())
    }

    "must redirect to the next page when valid data is submitted" in new Setup {
      val userAnswers: UserAnswers = emptyUserAnswers
      private val request          = FakeRequest().withFormUrlEncodedBody(("value", "1234567890"))

      when(mockSessionRepository.set(any[UserAnswers]))
        .thenReturn(Future.successful(true))

      private val result = controllerUnderTest.onSubmit(userAnswersId, NormalMode)(request)

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual onwardRoute.url
      verify(mockSessionRepository).set(any())
    }

    "must return a Bad Request and errors when invalid data is submitted" in new Setup {
      val userAnswers: UserAnswers = emptyUserAnswers
      private val request          = FakeRequest().withFormUrlEncodedBody(("value", "123456789"))
      private val boundForm        = form.bind(Map("value" -> "123456789"))

      private val result = controllerUnderTest.onSubmit(userAnswersId, NormalMode)(request)

      status(result) mustEqual BAD_REQUEST
      contentAsString(result) mustEqual mockContent

      verify(mockView).apply(
        eqTo(boundForm),
        eqTo(userAnswersId),
        eqTo(NormalMode)
      )(any(), any())
    }
  }

  abstract private class Setup {
    protected val userAnswers: UserAnswers

    protected val mockSessionRepository: SessionRepository = mock[SessionRepository]
    protected val mockView: EnterUTRView                   = mock
    protected val mockContent                              = "EnterUTR"
    private val enterUTRFormProvider                       = new EnterUTRFormProvider()
    protected val form: Form[String]                       = enterUTRFormProvider()

    when(mockView(any(), any(), any())(any(), any()))
      .thenReturn(Html(mockContent))

    protected lazy val controllerUnderTest = new EnterUTRController(
      mockSessionRepository,
      new Cwf1Navigator with FakeNavigator(onwardRoute),
      enterUTRFormProvider,
      new MockAppControllerComponents(userAnswers),
      mockView
    )
  }
}
