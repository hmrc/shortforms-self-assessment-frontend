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

import models.{JourneyType, UserAnswers}
import play.api.libs.json.Json
import repositories.SessionRepository

import java.time.Clock
import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class StartJourneyService @Inject() (
  sessionRepository: SessionRepository,
  clock: Clock
)(implicit ec: ExecutionContext) {

  def startJourney(
    userId: String,
    journeyType: JourneyType
  ): Future[String] = {

    val journeyId = UUID.randomUUID().toString

    val userAnswers = UserAnswers(
      id = journeyId,
      userId = userId,
      journeyType = journeyType,
      data = Json.obj(),
      lastUpdated = clock.instant()
    )

    sessionRepository.set(userAnswers).map(_ => journeyId)
  }
}
