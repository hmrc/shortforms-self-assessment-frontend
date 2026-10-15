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
import forms.sa1.Sa1NameFormProvider
import models.UserAnswers
import models.sa1.Sa1Name
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{mock, verify, when}
import pages.sa1.Sa1NamePage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.sa1.Sa1NameView

import scala.concurrent.Future

class Sa1NameControllerSpec extends SpecBase {

  private val formProvider = new Sa1NameFormProvider()
  private val validData = Map("firstName" -> "Jane", "lastName" -> "Doe")

  "Sa1Name Controller" - {

    "must return OK and the correct view for a GET" in {
      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(
          GET,
          routes.Sa1NameController.onPageLoad(userAnswersId).url
        )

        val result = route(application, request).value

        val view = application.injector.instanceOf[Sa1NameView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(formProvider(), userAnswersId)(request, messages(application)).toString
      }
    }

    "must populate the view with the saved name on a GET when the question has been answered" in {
      val answers = emptyUserAnswers.set(Sa1NamePage, Sa1Name("Jane", "Doe")).success.value

      val application =
        applicationBuilder(userAnswers = Some(answers)).build()

      running(application) {
        val request = FakeRequest(
          GET,
          routes.Sa1NameController.onPageLoad(userAnswersId).url
        )

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) must include("value=\"Jane\"")
        contentAsString(result) must include("value=\"Doe\"")
      }
    }

    "must redirect to Journey Recovery for a GET when no existing data is found" in {
      val application =
        applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(
          GET,
          routes.Sa1NameController.onPageLoad(userAnswersId).url
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
          routes.Sa1NameController.onSubmit(userAnswersId).url
        ).withFormUrlEncodedBody(validData.toSeq*)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.routes.IndexController.onPageLoad().url
        verify(mockSessionRepository).set(any[UserAnswers])
      }
    }

    "must return a Bad Request and show errors when invalid data is submitted" in {
      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(
          POST,
          routes.Sa1NameController.onSubmit(userAnswersId).url
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
          routes.Sa1NameController.onSubmit(userAnswersId).url
        ).withFormUrlEncodedBody(validData.toSeq*)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
