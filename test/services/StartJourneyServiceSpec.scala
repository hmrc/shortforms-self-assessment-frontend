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

package services

import base.SpecBase
import models.JourneyType.SA1
import models.UserAnswers
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{mock, verify, when}
import play.api.libs.json.Json
import repositories.SessionRepository

import java.time.{Clock, Instant, ZoneOffset}
import scala.concurrent.Future

class StartJourneyServiceSpec extends SpecBase {
  private val userId       = testUserId
  private val fixedInstant = Instant.parse("2026-10-12T10:00:00Z")
  private val clock        = Clock.fixed(fixedInstant, ZoneOffset.UTC)

  "StartJourneyService" - {

    "must save a new UserAnswers and return the generated journey ID" in {
      val sessionRepository = mock(classOf[SessionRepository])

      when(sessionRepository.set(any[UserAnswers])).thenReturn(Future.successful(true))

      val service = new StartJourneyService(sessionRepository, clock)

      val journeyId = service.startJourney(userId, SA1).futureValue

      journeyId must not be empty

      val captor = ArgumentCaptor.forClass(classOf[UserAnswers])
      verify(sessionRepository).set(captor.capture())

      val savedAnswers = captor.getValue

      savedAnswers.id mustEqual journeyId
      savedAnswers.userId mustEqual userId
      savedAnswers.journeyType mustEqual SA1
      savedAnswers.data mustEqual Json.obj()
      savedAnswers.lastUpdated mustEqual fixedInstant
    }

    "must propagate a repository failure" in {
      val sessionRepository = mock(classOf[SessionRepository])
      val exception         = new RuntimeException("MongoDB failure")

      when(sessionRepository.set(any[UserAnswers]))
        .thenReturn(Future.failed(exception))

      val service = new StartJourneyService(sessionRepository, clock)

      val result = service.startJourney(userId, SA1)

      result.failed.futureValue mustBe exception
    }
  }
}
