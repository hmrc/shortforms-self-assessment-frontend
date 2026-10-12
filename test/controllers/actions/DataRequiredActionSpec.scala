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
import models.requests.IdentifierRequest
import org.mockito.ArgumentMatchers.eq as eqTo
import org.mockito.Mockito.{mock, verify, when}
import play.api.libs.json.Json
import play.api.mvc.Results.Ok
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository

import java.time.Instant
import scala.concurrent.Future

class DataRequiredActionSpec extends SpecBase {

  private val journeyId = "test-journey-id"
  private val userId = "test-user-id"

  private val userAnswers = UserAnswers(
    id = journeyId,
    userId = userId,
    journeyType = SA1,
    data = Json.obj("foo" -> "bar"),
    lastUpdated = Instant.parse("2026-10-12T10:00:00Z")
  )

  private val identifierRequest =
    IdentifierRequest(FakeRequest(GET, "/"), userId)

  "DataRequiredActionImpl" - {

    "must provide DataRequest when the journey exists for the user" in {
      val repository = mock(classOf[SessionRepository])

      when(repository.get(eqTo(journeyId), eqTo(userId), eqTo(SA1)))
        .thenReturn(Future.successful(Some(userAnswers)))

      val action = new DataRequiredActionImpl(repository)(ec)
        .apply(journeyId, SA1)

      val result = action.invokeBlock(
        identifierRequest,
        request => Future.successful(Ok(request.userAnswers.id))
      )

      status(result) mustEqual OK
      contentAsString(result) mustEqual journeyId

      verify(repository).get(journeyId, userId, SA1)
    }

    "must redirect to Journey Recovery when the journey does not exist" in {
      val repository = mock(classOf[SessionRepository])

      when(repository.get(eqTo(journeyId), eqTo(userId), eqTo(SA1)))
        .thenReturn(Future.successful(None))

      val action = new DataRequiredActionImpl(repository)(ec)
        .apply(journeyId, SA1)

      val result = action.invokeBlock(
        identifierRequest,
        _ => Future.successful(Ok)
      )

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual
        controllers.routes.JourneyRecoveryController.onPageLoad().url

      verify(repository).get(journeyId, userId, SA1)
    }
  }
}

