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

package repositories

import config.FrontendAppConfig
import models.{JourneyType, UserAnswers}
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.model.*
import play.api.libs.json.Format
import uk.gov.hmrc.mdc.Mdc
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

import java.time.{Clock, Instant}
import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SessionRepository @Inject() (
  mongoComponent: MongoComponent,
  appConfig: FrontendAppConfig,
  clock: Clock
)(implicit ec: ExecutionContext)
    extends PlayMongoRepository[UserAnswers](
      collectionName = "user-answers",
      mongoComponent = mongoComponent,
      domainFormat = UserAnswers.format,
      indexes = Seq(
        IndexModel(
          Indexes.ascending("lastUpdated"),
          IndexOptions()
            .name("lastUpdatedIdx")
            .expireAfter(appConfig.cacheTtl, TimeUnit.SECONDS)
        ),
        IndexModel(
          Indexes.ascending("userId"),
          IndexOptions().name("userIdIdx")
        )
      )
    ) {

  implicit val instantFormat: Format[Instant] =
    MongoJavatimeFormats.instantFormat

  private def byJourneyUserAndType(
    journeyId: String,
    userId: String,
    journeyType: JourneyType
  ): Bson =
    Filters.and(
      Filters.equal("_id", journeyId),
      Filters.equal("userId", userId),
      Filters.equal("journeyType", journeyType.toString)
    )

  def keepAlive(
    journeyId: String,
    userId: String,
    journeyType: JourneyType
  ): Future[Boolean] =
    collection
      .updateOne(
        filter = byJourneyUserAndType(journeyId, userId, journeyType),
        update = Updates.set("lastUpdated", Instant.now(clock))
      )
      .toFuture()
      .map(_ => true)

  def get(journeyId: String, userId: String, journeyType: JourneyType): Future[Option[UserAnswers]] =
    keepAlive(journeyId, userId, journeyType).flatMap { _ =>
      Mdc.preservingMdc {
        collection
          .find(byJourneyUserAndType(journeyId, userId, journeyType))
          .headOption()
      }
    }

  def get(journeyId: String, userId: String): Future[Option[UserAnswers]] =
    Mdc.preservingMdc {
      collection
        .findOneAndUpdate(
          filter = Filters.and(Filters.equal("_id", journeyId), Filters.equal("userId", userId)),
          update = Updates.set("lastUpdated", Instant.now(clock)),
          options = FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER)
        )
        .headOption()
    }

  def set(answers: UserAnswers): Future[Boolean] = {
    val updatedAnswers = answers copy (lastUpdated = Instant.now(clock))
    val filter         =
      byJourneyUserAndType(
        updatedAnswers.id,
        updatedAnswers.userId,
        updatedAnswers.journeyType
      )
    collection
      .replaceOne(
        filter = filter,
        replacement = updatedAnswers,
        options = ReplaceOptions().upsert(true)
      )
      .toFuture()
      .map(_ => true)
  }

  def clear(
    journeyId: String,
    userId: String,
    journeyType: JourneyType
  ): Future[Boolean] =
    collection
      .deleteOne(byJourneyUserAndType(journeyId, userId, journeyType))
      .toFuture()
      .map(_ => true)

  def clearByUserId(userId: String): Future[Boolean] =
    collection
      .deleteMany(Filters.equal("userId", userId))
      .toFuture()
      .map(_ => true)
}
