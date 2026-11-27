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
import forms.cwf1.AreYouUkResidentFormProvider
import models.{NormalMode, UserAnswers}
import navigation.{Cwf1Navigator, FakeNavigator}
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{verify, when}
import org.scalactic.Prettifier.default
import org.scalatestplus.mockito.MockitoSugar
import pages.cwf1.AreYouUkResidentPage
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import play.twirl.api.Html
import repositories.SessionRepository
import views.html.cwf1.AreYouUkResidentView

import scala.concurrent.Future

class AreYouUkResidentControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  "AreYouUkResident Controller" - {

    "must return OK and the correct view for a GET" in new Setup(
      emptyUserAnswers
    ) {
      val result = controllerUnderTest.onPageLoad(userAnswersId, NormalMode)(FakeRequest())

      status(result) mustEqual OK
      contentAsString(result) mustEqual mockContent

      verify(mockView).apply(
        eqTo(form),
        eqTo(userAnswersId),
        eqTo(NormalMode)
      )(any(), any())
    }

    "must populate the view correctly on a GET when the question has previously been answered" in new Setup(
      emptyUserAnswers.set(AreYouUkResidentPage, true).success.value
    ) {
      val result = controllerUnderTest.onPageLoad(userAnswersId, NormalMode)(FakeRequest())

      status(result) mustEqual OK
      contentAsString(result) mustEqual mockContent

      verify(mockView).apply(
        eqTo(form.fill(true)),
        eqTo(userAnswersId),
        eqTo(NormalMode)
      )(any(), any())
    }

    "must redirect to the next page when valid data is submitted" in new Setup(
      emptyUserAnswers
    ) {
      val request =
        FakeRequest()
          .withFormUrlEncodedBody(("value", "true"))

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val result = controllerUnderTest.onSubmit(userAnswersId, NormalMode)(request)

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual onwardRoute.url

      verify(mockSessionRepository).set(any())
    }

    "must return a Bad Request and errors when invalid data is submitted" in new Setup(
      emptyUserAnswers
    ) {
      val request =
        FakeRequest()
          .withFormUrlEncodedBody(("value", ""))

      val boundForm = form.bind(Map("value" -> ""))

      val result = controllerUnderTest.onSubmit(userAnswersId, NormalMode)(request)

      status(result) mustEqual BAD_REQUEST
      contentAsString(result) mustEqual mockContent

      verify(mockView).apply(
        eqTo(boundForm),
        eqTo(userAnswersId),
        eqTo(NormalMode)
      )(any(), any())
    }
  }

  private class Setup(
    userAnswers: UserAnswers = emptyUserAnswers
  ) {
    protected val mockView: AreYouUkResidentView = mock[AreYouUkResidentView]
    protected val mockContent                    = "AreYouUkResidentView"

    when(mockView(any(), any(), any())(any(), any()))
      .thenReturn(Html(mockContent))

    private val formProvider = new AreYouUkResidentFormProvider()
    protected val form       = formProvider()

    protected val mockSessionRepository: SessionRepository = mock[SessionRepository]

    protected lazy val controllerUnderTest = new AreYouUkResidentController(
      new MockAppControllerComponents(userAnswers),
      mockSessionRepository,
      new Cwf1Navigator with FakeNavigator(onwardRoute),
      formProvider,
      mockView
    )
  }
}
