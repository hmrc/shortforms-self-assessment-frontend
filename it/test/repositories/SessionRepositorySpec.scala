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
import models.JourneyType.SA1
import models.UserAnswers
import models.address.{AddressDetails, ConfirmedAddress, Country}
import org.mockito.Mockito.when
import org.mongodb.scala.model.Filters
import org.scalactic.source.Position
import org.scalatest.OptionValues
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import org.slf4j.MDC
import pages.AddressPage
import play.api.libs.json.Json
import uk.gov.hmrc.mdc.MdcExecutionContext
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport

import java.time.temporal.ChronoUnit
import java.time.{Clock, Instant, ZoneId}
import scala.concurrent.{ExecutionContext, Future}

class SessionRepositorySpec
    extends AnyFreeSpec
    with Matchers
    with DefaultPlayMongoRepositorySupport[UserAnswers]
    with ScalaFutures
    with IntegrationPatience
    with OptionValues
    with MockitoSugar {

  private val instant          = Instant.now.truncatedTo(ChronoUnit.MILLIS)
  private val stubClock: Clock = Clock.fixed(instant, ZoneId.systemDefault)

  private val userId        = "test-user-id"
  private val anotherUserId = "another-user-id"

  private val userAnswers = UserAnswers(
    id = "test-journey-id",
    userId = userId,
    journeyType = SA1,
    data = Json.obj("foo" -> "bar"),
    lastUpdated = Instant.ofEpochSecond(1)
  )

  private val mockAppConfig = mock[FrontendAppConfig]
  when(mockAppConfig.cacheTtl) thenReturn 1L

  implicit val productionLikeTestMdcExecutionContext: ExecutionContext = MdcExecutionContext()

  protected override val repository: SessionRepository = new SessionRepository(
    mongoComponent = mongoComponent,
    appConfig = mockAppConfig,
    clock = stubClock
  )

  ".set" - {

    "must set the last updated time on the supplied user answers to `now`, and save them" in {

      val expectedResult = userAnswers copy (lastUpdated = instant)

      repository.set(userAnswers).futureValue
      val updatedRecord = find(Filters.equal("_id", userAnswers.id)).futureValue.headOption.value

      updatedRecord mustEqual expectedResult
    }

    mustPreserveMdc(repository.set(userAnswers))

    "must persist the confirmed address in data.address and retain it across generic and SA1 reads" in {
      val address = ConfirmedAddress(
        address = AddressDetails(
          lines = Seq("1 Test Street"),
          town = Some("London"),
          postcode = Some("SW1A 1AA"),
          country = Some(Country("GB", "United Kingdom"))
        ),
        auditRef = "alf-audit-ref",
        uprn = Some(123456789L)
      )
      val updatedAnswers = userAnswers.set(AddressPage, address).get

      repository.set(updatedAnswers).futureValue mustBe true

      val storedAnswers = repository.get(userAnswers.id, userId).futureValue.value
      storedAnswers.get(AddressPage).value mustBe address
      (storedAnswers.data \ "address").as[ConfirmedAddress] mustBe address
      (storedAnswers.data \ "foo").as[String] mustBe "bar"
      repository.get(userAnswers.id, userId, SA1).futureValue.value.get(AddressPage).value mustBe address
    }
  }

  ".get(journeyId, userId, journeyType)" - {

    "must update lastUpdated and return the matching record" in {
      insert(userAnswers).futureValue

      val result =
        repository.get(userAnswers.id, userId, SA1).futureValue

      result.value mustEqual userAnswers.copy(lastUpdated = instant)
    }

    ".get(journeyId, userId)" - {
      models.JourneyType.values.foreach { journeyType =>
        s"must return the stored $journeyType journey and refresh lastUpdated" in {
          val answers = userAnswers.copy(journeyType = journeyType)
          insert(answers).futureValue
          repository.get(answers.id, userId).futureValue.value mustEqual answers.copy(lastUpdated = instant)
        }
      }

      "must not read or refresh another user's journey" in {
        insert(userAnswers).futureValue
        repository.get(userAnswers.id, anotherUserId).futureValue mustBe None
        find(Filters.equal("_id", userAnswers.id)).futureValue.headOption.value mustEqual userAnswers
      }

      "must return None when the journey is missing" in {
        repository.get("missing-journey-id", userId).futureValue mustBe None
      }

      mustPreserveMdc(repository.get(userAnswers.id, userId))
    }

    "must return None when the record does not exist" in {
      repository
        .get("missing-journey-id", userId, SA1)
        .futureValue must not be defined
    }

    "must return None when the journey belongs to another user" in {
      insert(userAnswers).futureValue

      repository
        .get(userAnswers.id, anotherUserId, SA1)
        .futureValue must not be defined
    }

    "must return None when the journey type does not match" in {
      insert(userAnswers).futureValue

      repository
        .get(userAnswers.id, userId, models.JourneyType.CWF1ST)
        .futureValue must not be defined
    }

    mustPreserveMdc(repository.get(userAnswers.id, userId, SA1))
  }

  ".clearByUserId" - {

    "must remove all journeys belonging to the user" in {
      val secondAnswers = userAnswers.copy(
        id = "second-journey-id",
        data = Json.obj("another" -> "answer")
      )

      val otherUsersAnswers = userAnswers.copy(
        id = "other-users-journey-id",
        userId = anotherUserId
      )

      insert(userAnswers).futureValue
      insert(secondAnswers).futureValue
      insert(otherUsersAnswers).futureValue

      repository.clearByUserId(userId).futureValue mustEqual true

      repository
        .get(userAnswers.id, userId, SA1)
        .futureValue must not be defined

      repository
        .get(secondAnswers.id, userId, SA1)
        .futureValue must not be defined

      repository
        .get(otherUsersAnswers.id, anotherUserId, SA1)
        .futureValue mustBe defined
    }

    "must return true when no records are deleted" in {
      repository.clearByUserId("missing-user-id").futureValue mustEqual true
    }

    mustPreserveMdc(repository.clearByUserId(userId))
  }

  ".clear(journeyId, userId, journeyType)" - {

    "must remove the journey belonging to the specified user" in {
      insert(userAnswers).futureValue

      val result =
        repository.clear(userAnswers.id, userId, SA1).futureValue

      result mustEqual true

      repository
        .get(userAnswers.id, userId, SA1)
        .futureValue must not be defined
    }

    "must not remove a journey belonging to another user" in {
      insert(userAnswers).futureValue

      val result =
        repository.clear(userAnswers.id, anotherUserId, SA1).futureValue

      result mustEqual true

      repository
        .get(userAnswers.id, userId, SA1)
        .futureValue mustBe defined
    }

    "must not remove a journey when the journey type does not match" in {
      insert(userAnswers).futureValue

      repository
        .clear(userAnswers.id, userId, models.JourneyType.CWF1ST)
        .futureValue mustEqual true

      repository
        .get(userAnswers.id, userId, SA1)
        .futureValue mustBe defined
    }

    mustPreserveMdc(repository.clear(userAnswers.id, userId, SA1))
  }

  ".keepAlive(journeyId, userId, journeyType)" - {

    "must update lastUpdated and return true when the record exists" in {
      insert(userAnswers).futureValue

      val result =
        repository.keepAlive(userAnswers.id, userId, SA1).futureValue

      result mustEqual true

      val updatedAnswers =
        find(Filters.equal("_id", userAnswers.id)).futureValue.headOption.value

      updatedAnswers mustEqual userAnswers.copy(lastUpdated = instant)
    }

    "must return true when the record does not exist" in {
      repository
        .keepAlive("missing-journey-id", userId, SA1)
        .futureValue mustEqual true
    }

    "must not update a journey belonging to another user" in {
      insert(userAnswers).futureValue

      repository
        .keepAlive(userAnswers.id, anotherUserId, SA1)
        .futureValue mustEqual true

      find(Filters.equal("_id", userAnswers.id)).futureValue.headOption.value mustEqual userAnswers
    }

    mustPreserveMdc(repository.keepAlive(userAnswers.id, userId, SA1))
  }

  private def mustPreserveMdc[A](f: => Future[A])(implicit pos: Position): Unit =
    "must preserve MDC" in {

      MDC.put("test", "foo")

      (f.map { _ =>
        Option(MDC.get("test"))
      }.futureValue) mustEqual Some("foo")
    }
}
