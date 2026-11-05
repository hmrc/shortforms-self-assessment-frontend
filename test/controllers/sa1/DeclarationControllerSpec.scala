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
import forms.sa1.DeclarationFormProvider
import models.{JourneyType, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{mock, never, verify, when}
import pages.sa1.DeclarationPage
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.Request
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import play.twirl.api.Html
import repositories.SessionRepository
import views.html.sa1.DeclarationView

import scala.concurrent.Future

class DeclarationControllerSpec extends SpecBase {

  "DeclarationController" - {

    "must pre-populate the name when loading an answered declaration" in new Setup {
      override protected val userAnswers: UserAnswers =
        UserAnswers(userAnswersId, testUserId, JourneyType.SA1).set(DeclarationPage, "Jane Doe").success.value

      val result = controller.onPageLoad(userAnswersId)(FakeRequest())

      status(result) mustBe OK
      capturedForm().value mustBe Some("Jane Doe")
    }

    "must show errors and not save a name longer than 100 characters" in new Setup {
      val result = controller.onSubmit(userAnswersId)(FakeRequest().withFormUrlEncodedBody("value" -> "A" * 101))

      status(result) mustBe BAD_REQUEST
      capturedForm().errors.map(_.message) mustBe Seq("sa1.declaration.name.error.length")
      verify(sessionRepository, never()).set(any[UserAnswers])
    }

    "must save a valid name and remain on the declaration page" in new Setup {
      val result = controller.onSubmit(userAnswersId)(FakeRequest().withFormUrlEncodedBody("value" -> "Jane Doe"))

      status(result) mustBe SEE_OTHER
      redirectLocation(result).value mustBe routes.DeclarationController.onPageLoad(userAnswersId).url

      val savedAnswers = ArgumentCaptor.forClass(classOf[UserAnswers])
      verify(sessionRepository).set(savedAnswers.capture())
      savedAnswers.getValue.get(DeclarationPage) mustBe Some("Jane Doe")
    }
  }

  private class Setup {
    protected val userAnswers: UserAnswers = emptyUserAnswers

    protected val sessionRepository: SessionRepository = mock(classOf[SessionRepository])
    when(sessionRepository.set(any[UserAnswers])).thenReturn(Future.successful(true))

    private val view: DeclarationView = mock(classOf[DeclarationView])
    when(view.apply(any[Form[String]], any[String])(any[Request[?]], any[Messages]))
      .thenReturn(Html("Declaration view"))

    protected lazy val controller = new DeclarationController(
      new MockAppControllerComponents(userAnswers),
      sessionRepository,
      new DeclarationFormProvider(),
      view
    )

    protected def capturedForm(): Form[String] = {
      val captor = ArgumentCaptor.forClass(classOf[Form[String]])
      verify(view).apply(captor.capture(), any[String])(any[Request[?]], any[Messages])
      captor.getValue
    }
  }
}
