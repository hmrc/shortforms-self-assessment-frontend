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
import models.{JourneyType, SubmissionConfirmation, UserAnswers}
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{mock, verify, when}
import pages.sa1.DeclarationPage
import play.api.i18n.Messages
import play.api.mvc.{Request, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import play.twirl.api.Html
import repositories.SessionRepository
import views.html.PageNotFoundView
import views.html.sa1.ConfirmationView

import java.time.Instant
import scala.concurrent.Future

class ConfirmationControllerSpec extends SpecBase {

  val confirmation: SubmissionConfirmation         = SubmissionConfirmation(
    "AB123CD",
    Instant.now()
  )
  private val sessionRepository: SessionRepository = mock(classOf[SessionRepository])

  "ConfirmationController" - {

    "post successful declaration with valid submission" - {

      val userAnswers: UserAnswers =
        UserAnswers(userAnswersId, testUserId, JourneyType.SA1, confirmation = Some(confirmation))
          .set(DeclarationPage, "Jane Doe")
          .success
          .value

      "must render the page with the current UK date and time" in new Setup(userAnswers) {

        val result: Future[Result] = controller.onPageLoad(userAnswersId)(FakeRequest())

        status(result) mustBe OK
        contentAsString(result) mustBe "Confirmation view"

        verify(view).apply(
          any,
          eqTo("AB123CD")
        )(any[Request[?]], any[Messages])

        verify(sessionRepository).clear(any, any, any)
      }

      "verify the clear method invoke successfully post the confirmation request to clear session data" in new Setup(
        userAnswers
      ) {

        val result: Future[Result] = controller.onPageLoad(userAnswersId)(FakeRequest())

        status(result) mustBe OK

        verify(sessionRepository).clear(any, any, any)

        verify(view).apply(
          any,
          eqTo("AB123CD")
        )(any[Request[?]], any[Messages])

      }
    }

    "check service guard without valid request or userAnswers page" - {

      "must render page not found page when SubmissionConfirmation is none" in new Setup(emptyUserAnswers) {
        val result: Future[Result] = controller.onPageLoad(userAnswersId)(FakeRequest())

        status(result) mustBe OK
        contentAsString(result) mustBe "Page not found"

      }
    }

  }

  private class Setup(userAnswers: UserAnswers) {
    protected val view: ConfirmationView           = mock(classOf[ConfirmationView])
    private val pageNotFoundView: PageNotFoundView = mock(classOf[PageNotFoundView])
    when(sessionRepository.clear(any[String], any[String], any)).thenReturn(Future.successful(true))

    private val expectedReferenceNumber = "AB123CD"
    when(view.apply(any[String], eqTo(expectedReferenceNumber))(any[Request[?]], any[Messages]))
      .thenReturn(Html("Confirmation view"))
    when(pageNotFoundView.apply()(any[Request[?]], any[Messages]))
      .thenReturn(Html("Page not found"))

    protected val controller = new ConfirmationController(
      new MockAppControllerComponents(userAnswers = userAnswers),
      sessionRepository,
      view,
      pageNotFoundView
    )
  }
}
