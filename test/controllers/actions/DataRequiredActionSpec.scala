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

package controllers.actions

import base.SpecBase
import models.JourneyType.SA1
import models.UserAnswers
import models.requests.{DataRequest, IdentifierRequest}
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.Json
import play.api.mvc.ActionRefiner
import play.api.mvc.Results.Ok
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.ErrorTemplate

import java.time.Instant
import scala.concurrent.Future

class DataRequiredActionSpec extends SpecBase with MockitoSugar {

  private val journeyId = "test-journey-id"
  private val userId    = "test-user-id"
  private val nino      = Some("NM772208B")

  private val userAnswers = UserAnswers(
    id = journeyId,
    userId = userId,
    journeyType = SA1,
    data = Json.obj("foo" -> "bar"),
    lastUpdated = Instant.parse("2026-10-12T10:00:00Z")
  )

  private val identifierRequest = IdentifierRequest(FakeRequest(), userId, nino)

  "DataRequiredActionImpl" - {

    "must provide DataRequest when the journey exists for the user" in new Setup {
      when(repository.get(eqTo(journeyId), eqTo(userId), eqTo(SA1)))
        .thenReturn(Future.successful(Some(userAnswers)))

      private val result = actionUnderTest.invokeBlock(
        identifierRequest,
        request => Future.successful(Ok(request.userAnswers.id))
      )

      status(result) mustEqual OK
      contentAsString(result) mustEqual journeyId

      verify(repository).get(journeyId, userId, SA1)
    }

    "must return Not Found when the journey does not exist" in new Setup {
      when(repository.get(eqTo(journeyId), eqTo(userId), eqTo(SA1)))
        .thenReturn(Future.successful(None))

      private val result = actionUnderTest.invokeBlock(
        identifierRequest,
        _ => Future.successful(Ok)
      )

      status(result) mustEqual NOT_FOUND
      contentAsString(result) mustEqual mockErrorContent

      verify(repository).get(journeyId, userId, SA1)
    }
  }

  private class Setup {
    protected val repository: SessionRepository = mock

    private val mockErrorTemplate: ErrorTemplate = mock
    protected val mockErrorContent               = "Error"
    when(mockErrorTemplate.apply(any, any, any)(any, any)) thenReturn play.twirl.api.Html(mockErrorContent)

    protected val actionUnderTest: ActionRefiner[IdentifierRequest, DataRequest] =
      new DataRequiredActionImpl(repository, mockErrorTemplate, stubMessagesApi()).apply(journeyId, SA1)
  }
}
