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
import forms.sa1.NoNinoReasonFormProvider
import models.{NormalMode, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.sa1.NoNinoReasonPage
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.{AnyContentAsFormUrlEncoded, Request}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import play.twirl.api.Html
import repositories.SessionRepository
import views.html.sa1.NoNinoReasonView

import scala.concurrent.Future

class NoNinoReasonControllerSpec extends SpecBase with MockitoSugar {

  private val noNinoReason = "No Nino"

  "NoNinoReasonController" - {

    "onPageLoad" - {

      "must return OK and display the page" in new Setup {
        val result = controllerUnderTest.onPageLoad(userAnswersId, NormalMode)(FakeRequest())

        status(result) mustEqual OK
        contentAsString(result) mustEqual mockContent
      }

      "must pre-populate the form when a reason already exists" in new Setup(
        emptyUserAnswers.set(NoNinoReasonPage, noNinoReason).success.value
      ) {
        val result = controllerUnderTest.onPageLoad(userAnswersId, NormalMode)(FakeRequest())
        status(result) mustEqual OK

        val captor: ArgumentCaptor[Form[String]] = ArgumentCaptor.forClass(classOf[Form[String]])
        verify(mockView).apply(captor.capture(), eqTo(userAnswersId))(any[Request[?]], any[Messages])

        val populatedForm: Form[String] = captor.getValue
        populatedForm("value").value mustBe Some(noNinoReason)
      }
    }

    "onSubmit" - {

      "must return BAD_REQUEST when the submitted form is invalid" in new Setup {
        val request: FakeRequest[AnyContentAsFormUrlEncoded] =
          FakeRequest(POST, "/").withFormUrlEncodedBody("value" -> "")

        val result = controllerUnderTest.onSubmit(userAnswersId, NormalMode)(request)

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual mockContent
      }

      "must save the submitted data and redirect when the form is valid" in new Setup {
        when(sessionRepository.set(any[UserAnswers])) thenReturn Future.successful(true)

        val request: FakeRequest[AnyContentAsFormUrlEncoded] =
          FakeRequest(POST, "/").withFormUrlEncodedBody("value" -> noNinoReason)

        val result = controllerUnderTest.onSubmit(userAnswersId, NormalMode)(request)

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          routes.NoNinoReasonController.onPageLoad(userAnswersId, NormalMode).url

        val captor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])

        verify(sessionRepository).set(captor.capture())
        captor.getValue.get(NoNinoReasonPage) mustBe Some(noNinoReason)
      }
    }
  }

  private class Setup(
    userAnswers: UserAnswers = emptyUserAnswers
  ) {
    protected val mockContent                = "NoNinoReasonView"
    protected val mockView: NoNinoReasonView = mock

    when(mockView.apply(any[Form[String]], any[String])(any[Request[?]], any[Messages])).thenReturn(Html(mockContent))

    protected val sessionRepository: SessionRepository = mock
    private val formProvider                           = new NoNinoReasonFormProvider()

    protected lazy val controllerUnderTest = new NoNinoReasonController(
      new MockAppControllerComponents(userAnswers),
      formProvider,
      sessionRepository,
      mockView
    )
  }
}
