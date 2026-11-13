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

import base.{SA1UserAnswersFixtures, SpecBase}
import connectors.SA1SubmissionConnector
import controllers.MockAppControllerComponents
import forms.sa1.DeclarationFormProvider
import models.sa1.{SA1Response, SA1Submission}
import models.UserAnswers
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
import uk.gov.hmrc.http.HeaderCarrier
import views.html.sa1.DeclarationView

import java.time.Instant
import scala.concurrent.Future

class DeclarationControllerSpec extends SpecBase {

  "DeclarationController" - {

    "must pre-populate the name when loading an answered declaration" in new Setup {
      override protected val userAnswers: UserAnswers =
        SA1UserAnswersFixtures.completeAnswers(userAnswersId, testUserId).set(DeclarationPage, "Jane Doe").success.value

      val result = controller.onPageLoad(userAnswersId)(FakeRequest())

      status(result) mustBe OK
      capturedForm().value mustBe Some("Jane Doe")
    }

    "must show errors and not save a name longer than 100 characters" in new Setup {
      val result = controller.onSubmit(userAnswersId)(FakeRequest().withFormUrlEncodedBody("value" -> "A" * 101))

      status(result) mustBe BAD_REQUEST
      capturedForm().errors.map(_.message) mustBe Seq("sa1.declaration.name.error.length")
      verify(sessionRepository, never()).set(any[UserAnswers])
      verify(submissionConnector, never()).submit(any[SA1Submission])(any[HeaderCarrier])
    }

    "must save the declaration, submit the SA1 payload and redirect to the confirmation page with the acknowledgement reference" in new Setup {
      val result = controller.onSubmit(userAnswersId)(FakeRequest().withFormUrlEncodedBody("value" -> "Jane Doe"))

      status(result) mustBe SEE_OTHER
      redirectLocation(result).value mustBe routes.ConfirmationController.onPageLoad().url
      flash(result).get("submissionReference") mustBe Some("ack-123")
      flash(result).get("submissionTimestamp") mustBe Some("2026-11-13T14:18:34.301Z")

      val savedAnswers = ArgumentCaptor.forClass(classOf[UserAnswers])
      verify(sessionRepository).set(savedAnswers.capture())
      savedAnswers.getValue.get(DeclarationPage) mustBe Some("Jane Doe")

      verify(submissionConnector).submit(any[SA1Submission])(any[HeaderCarrier])
    }

    "must accept an empty name because the declaration name is optional" in new Setup {
      val result = controller.onSubmit(userAnswersId)(FakeRequest().withFormUrlEncodedBody("value" -> ""))

      status(result) mustBe SEE_OTHER
      redirectLocation(result).value mustBe routes.ConfirmationController.onPageLoad().url

      val savedAnswers = ArgumentCaptor.forClass(classOf[UserAnswers])
      verify(sessionRepository).set(savedAnswers.capture())
      savedAnswers.getValue.get(DeclarationPage) mustBe Some("")
    }

    "must redirect to the system error page when the backend submission fails" in new Setup {
      when(submissionConnector.submit(any[SA1Submission])(any[HeaderCarrier]))
        .thenReturn(Future.failed(new RuntimeException("backend unavailable")))

      val result = controller.onSubmit(userAnswersId)(FakeRequest().withFormUrlEncodedBody("value" -> "Jane Doe"))

      status(result) mustBe SEE_OTHER
      redirectLocation(result).value mustBe controllers.routes.SystemErrorController.onPageLoad().url
      flash(result).get("submissionReference") mustBe None
    }

    "must redirect to the system error page without calling the backend when the answers are incomplete" in new Setup {
      override protected val userAnswers: UserAnswers = emptyUserAnswers

      val result = controller.onSubmit(userAnswersId)(FakeRequest().withFormUrlEncodedBody("value" -> "Jane Doe"))

      status(result) mustBe SEE_OTHER
      redirectLocation(result).value mustBe controllers.routes.SystemErrorController.onPageLoad().url
      verify(submissionConnector, never()).submit(any[SA1Submission])(any[HeaderCarrier])
    }
  }

  private class Setup {
    protected val userAnswers: UserAnswers = SA1UserAnswersFixtures.completeAnswers(userAnswersId, testUserId)

    protected val sessionRepository: SessionRepository = mock(classOf[SessionRepository])
    when(sessionRepository.set(any[UserAnswers])).thenReturn(Future.successful(true))

    protected val submissionConnector: SA1SubmissionConnector = mock(classOf[SA1SubmissionConnector])
    when(submissionConnector.submit(any[SA1Submission])(any[HeaderCarrier]))
      .thenReturn(Future.successful(SA1Response("ack-123", Instant.parse("2026-11-13T14:18:34.301Z"))))

    private val view: DeclarationView = mock(classOf[DeclarationView])
    when(view.apply(any[Form[String]], any[String])(any[Request[?]], any[Messages]))
      .thenReturn(Html("Declaration view"))

    protected lazy val controller = new DeclarationController(
      new MockAppControllerComponents(userAnswers),
      sessionRepository,
      new DeclarationFormProvider(),
      submissionConnector,
      view
    )

    protected def capturedForm(): Form[String] = {
      val captor = ArgumentCaptor.forClass(classOf[Form[String]])
      verify(view).apply(captor.capture(), any[String])(any[Request[?]], any[Messages])
      captor.getValue
    }
  }
}
