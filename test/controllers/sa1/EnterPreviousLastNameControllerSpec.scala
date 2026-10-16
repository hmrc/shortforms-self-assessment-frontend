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
import forms.PreviousNameFormProvider
import models.{NormalMode, PreviousName, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.sa1.PreviousLastNamePage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.sa1.EnterPreviousLastNameView

import java.time.LocalDate
import scala.concurrent.Future

class EnterPreviousLastNameControllerSpec extends SpecBase {
  private val journeyId = userAnswersId

  "Enter Previous Last Name Controller" - {

    "must return OK and the correct view for a GET" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
      val formProvider = application.injector.instanceOf[PreviousNameFormProvider]
      val form = formProvider()(messages(application))

      running(application) {
        val request = FakeRequest(GET, routes.EnterPreviousLastNameController.onPageLoad(journeyId, NormalMode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[EnterPreviousLastNameView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, journeyId)(request, messages(application)).toString
      }
    }

    "must pre-populate the form when previous name data exists" in {
      val previousName = PreviousName(
        previousLastName = "John Doe",
        lastNameChangeDate = LocalDate.of(2020, 5, 10)
      )

      val userAnswers = emptyUserAnswers.set(PreviousLastNamePage, previousName).success.value
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.EnterPreviousLastNameController.onPageLoad(journeyId, NormalMode).url)

        val result = route(application, request).value

        status(result) mustBe OK
        contentAsString(result) must include("John Doe")
      }
    }

    "must return NOT_FOUND for a GET if no existing data is found" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(
          GET,
          routes.EnterPreviousLastNameController
            .onPageLoad(journeyId, NormalMode).url
        )

        val result = route(application, request).value

        status(result) mustEqual NOT_FOUND
      }
    }

    "must return BAD_REQUEST when the submitted form is invalid" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(POST, routes.EnterPreviousLastNameController.onSubmit(journeyId, NormalMode).url)
          .withFormUrlEncodedBody(
            "previousLastName" -> "",
            "lastNameChangeDate.day" -> "10",
            "lastNameChangeDate.month" -> "5",
            "lastNameChangeDate.year" -> "2020"
          )

        val result = route(application, request).value
        status(result) mustEqual BAD_REQUEST
      }
    }

    "must save the submitted data and redirect when the form is valid" in {
      val mockSessionRepository = mock[SessionRepository]
      val captor = ArgumentCaptor.forClass(classOf[UserAnswers])

      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request = FakeRequest(POST, routes.EnterPreviousLastNameController.onSubmit(journeyId, NormalMode).url)
          .withFormUrlEncodedBody(
            "previousLastName" -> "John Doe",
            "lastNameChangeDate.day" -> "10",
            "lastNameChangeDate.month" -> "5",
            "lastNameChangeDate.year" -> "2020"
          )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
         //TODO: Change it to DOB page once its ready
          controllers.sa1.routes.EnterPreviousLastNameController.onPageLoad(journeyId, NormalMode).url

        verify(mockSessionRepository).set(captor.capture())

        val savedAnswers = captor.getValue
        savedAnswers.get(PreviousLastNamePage).value mustBe PreviousName(
          previousLastName = "John Doe",
          lastNameChangeDate = LocalDate.of(2020, 5, 10)
        )
      }
    }

    "must return NOT_FOUND for a POST if no existing data is found" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(
          POST,
          routes.EnterPreviousLastNameController
            .onSubmit(journeyId, NormalMode).url
        ).withFormUrlEncodedBody(
          "previousLastName" -> "John Doe",
          "lastNameChangeDate.day" -> "10",
          "lastNameChangeDate.month" -> "5",
          "lastNameChangeDate.year" -> "2020"
        )

        val result = route(application, request).value

        status(result) mustEqual NOT_FOUND
      }
    }
  }
}
