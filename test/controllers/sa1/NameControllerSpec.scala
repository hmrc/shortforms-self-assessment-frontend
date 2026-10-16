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
import forms.sa1.NameFormProvider
import models.{CheckMode, NormalMode, UserAnswers}
import models.sa1.Name
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{mock, verify, when}
import pages.sa1.NamePage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.sa1.NameView

import scala.concurrent.Future

class NameControllerSpec extends SpecBase {

  private val formProvider = new NameFormProvider()
  private val validData    = Map("firstName" -> "Jane", "lastName" -> "Doe")

  "NameController" - {

    "must return OK and the correct view for a GET" in {
      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(
          GET,
          routes.NameController.onPageLoad(userAnswersId, NormalMode).url
        )

        val result = route(application, request).value

        val view = application.injector.instanceOf[NameView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(formProvider(), userAnswersId, NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view with the saved name on a GET when the question has been answered" in {
      val answers = emptyUserAnswers.set(NamePage, Name("Jane", "Doe")).success.value

      val application =
        applicationBuilder(userAnswers = Some(answers)).build()

      running(application) {
        val request = FakeRequest(
          GET,
          routes.NameController.onPageLoad(userAnswersId, NormalMode).url
        )

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) must include("value=\"Jane\"")
        contentAsString(result) must include("value=\"Doe\"")
      }
    }

    "must return OK and render the change form action for a GET in check mode" in {
      val answers = emptyUserAnswers.set(NamePage, Name("Jane", "Doe")).success.value

      val application =
        applicationBuilder(userAnswers = Some(answers)).build()

      running(application) {
        val request = FakeRequest(
          GET,
          routes.NameController.onPageLoad(userAnswersId, CheckMode).url
        )

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) must include(
          routes.NameController.onSubmit(userAnswersId, CheckMode).url
        )
      }
    }

    "must redirect to Journey Recovery for a GET when no existing data is found" in {
      val application =
        applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(
          GET,
          routes.NameController.onPageLoad(userAnswersId, NormalMode).url
        )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must save the name and redirect to the next page when valid data is submitted" in {
      val mockSessionRepository = mock(classOf[SessionRepository])
      when(mockSessionRepository.set(any[UserAnswers])).thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

      running(application) {
        val request = FakeRequest(
          POST,
          routes.NameController.onSubmit(userAnswersId, NormalMode).url
        ).withFormUrlEncodedBody(validData.toSeq*)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          routes.NameController.onPageLoad(userAnswersId, NormalMode).url
        verify(mockSessionRepository).set(any[UserAnswers])
      }
    }

    "must redirect to Check your answers when valid data is submitted in check mode" in {
      val mockSessionRepository = mock(classOf[SessionRepository])
      when(mockSessionRepository.set(any[UserAnswers])).thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

      running(application) {
        val request = FakeRequest(
          POST,
          routes.NameController.onSubmit(userAnswersId, CheckMode).url
        ).withFormUrlEncodedBody(validData.toSeq*)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.sa1.routes.NameController.onPageLoad(userAnswersId, NormalMode).url
        verify(mockSessionRepository).set(any[UserAnswers])
      }
    }

    "must return a Bad Request and show errors when invalid data is submitted" in {
      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(
          POST,
          routes.NameController.onSubmit(userAnswersId, NormalMode).url
        ).withFormUrlEncodedBody("firstName" -> "", "lastName" -> "Doe")

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) must include(messages(application)("sa1.name.firstName.error.required"))
      }
    }

    "must redirect to Journey Recovery for a POST when no existing data is found" in {
      val application =
        applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(
          POST,
          routes.NameController.onSubmit(userAnswersId, NormalMode).url
        ).withFormUrlEncodedBody(validData.toSeq*)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
