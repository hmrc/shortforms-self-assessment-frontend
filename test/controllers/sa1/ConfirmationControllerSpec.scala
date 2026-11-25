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
import handlers.ErrorHandler
import models.{JourneyType, SubmissionConfirmation, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{mock, never, verify, when}
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.mvc.Results.NotFound
import play.api.mvc.{Request, RequestHeader, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import play.twirl.api.Html
import repositories.SessionRepository
import views.html.sa1.ConfirmationView

import java.time.Instant
import scala.concurrent.Future

class ConfirmationControllerSpec extends SpecBase {

  private val receivedTimestamp = Instant.parse("2026-11-13T14:18:34.301Z")
  private val confirmation      = SubmissionConfirmation("AB123CD", receivedTimestamp)

  "ConfirmationController" - {

    "must render the UK submission time and acknowledgement reference from the saved confirmation" in new Setup(
      UserAnswers(userAnswersId, testUserId, JourneyType.SA1, confirmation = Some(confirmation))
    ) {
      val result: Future[Result] = controller.onPageLoad(userAnswersId)(FakeRequest())

      status(result) mustBe OK
      contentAsString(result) mustBe "Confirmation view"
      verify(view).apply(eqTo("14:18 on 13 November 2026"), eqTo("AB123CD"))(any[Request[?]], any[Messages])
    }

    "must clear the submitted data from the session after rendering" in new Setup(
      UserAnswers(userAnswersId, testUserId, JourneyType.SA1, confirmation = Some(confirmation))
        .copy(data = Json.obj("declaration" -> "Jane Doe"))
    ) {
      val result: Future[Result] = controller.onPageLoad(userAnswersId)(FakeRequest())

      status(result) mustBe OK
      val savedAnswers = ArgumentCaptor.forClass(classOf[UserAnswers])
      verify(sessionRepository).set(savedAnswers.capture())
      savedAnswers.getValue.data mustBe Json.obj()
      savedAnswers.getValue.confirmation mustBe Some(confirmation)
    }

    "must return not found when there is no saved confirmation, such as after a page refresh" in new Setup(
      UserAnswers(userAnswersId, testUserId, JourneyType.SA1)
    ) {
      val result: Future[Result] = controller.onPageLoad(userAnswersId)(FakeRequest())

      status(result) mustBe NOT_FOUND
      contentAsString(result) mustBe "Page not found"
      verify(sessionRepository, never()).set(any[UserAnswers])
    }

    "must render page not found page when userAnswers is none" in new Setup(emptyUserAnswers) {
      when(sessionRepository.get(any, any, any)).thenReturn(Future.successful(None))

      val result: Future[Result] = controller.onPageLoad(userAnswersId)(FakeRequest())

      status(result) mustBe NOT_FOUND
      contentAsString(result) mustBe "Page not found"
    }
  }

  private class Setup(userAnswers: UserAnswers) {
    protected val view: ConfirmationView = mock(classOf[ConfirmationView])

    protected val sessionRepository: SessionRepository = mock(classOf[SessionRepository])
    when(sessionRepository.get(any, any, any)).thenReturn(Future.successful(Some(userAnswers)))
    when(sessionRepository.set(any[UserAnswers])).thenReturn(Future.successful(true))

    when(view.apply(any[String], any[String])(any[Request[?]], any[Messages]))
      .thenReturn(Html("Confirmation view"))

    private val mockErrorHandler: ErrorHandler = mock(classOf[ErrorHandler])
    private val mockErrorContent               = "Page not found"
    when(mockErrorHandler.onClientError(any[RequestHeader], eqTo(NOT_FOUND), any[String])) thenReturn Future.successful(
      NotFound(Html(mockErrorContent))
    )

    protected val controller = new ConfirmationController(
      new MockAppControllerComponents(userAnswers = userAnswers),
      sessionRepository,
      view,
      mockErrorHandler
    )
  }
}
