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

package viewmodels.checkAnswers.sa1

import base.SpecBase
import models.CheckMode
import models.sa1.PreviousName
import pages.sa1.{AddPreviousLastNamePage, PreviousLastNamePage}
import play.api.test.Helpers.stubMessages

import java.time.LocalDate

class EnterPreviousLastNameSummarySpec extends SpecBase {

  private val msgs = stubMessages()

  "EnterPreviousLastNameSummary" - {

    "must return no rows when the user has not added a previous name" in {
      val result =
        EnterPreviousLastNameSummary.rows(emptyUserAnswers)(msgs)

      result mustBe empty
    }

    "must return two rows with not provided values when previous name details are missing" in {
      val userAnswers =
        emptyUserAnswers
          .set(AddPreviousLastNamePage, true)
          .success
          .value

      val result =
        EnterPreviousLastNameSummary.rows(userAnswers)(msgs)

      result must have size 2

      result.head.value.content.toString must include(msgs("site.notProvided"))
      result(1).value.content.toString   must include(msgs("site.notProvided"))

      result.head.actions.value.items.head.content.toString must include(msgs("site.add"))
      result(1).actions.value.items.head.content.toString   must include(msgs("site.add"))

      val expectedUrl =
        controllers.sa1.routes.EnterPreviousLastNameController
          .onPageLoad(userAnswersId, CheckMode)
          .url

      result.head.actions.value.items.head.href mustBe expectedUrl
      result(1).actions.value.items.head.href mustBe expectedUrl
    }

    "must return two populated rows when previous name answers exist" in {
      val previousName = PreviousName(
        previousLastName = "John Doe",
        lastNameChangeDate = LocalDate.of(2020, 5, 10)
      )

      val userAnswers =
        emptyUserAnswers
          .set(AddPreviousLastNamePage, true)
          .success
          .value
          .set(PreviousLastNamePage, previousName)
          .success
          .value

      val result =
        EnterPreviousLastNameSummary.rows(userAnswers)(msgs)

      result must have size 2

      result.head.value.content.toString must include("John Doe")
      result(1).value.content.toString   must include("10 May 2020")

      result.head.actions.value.items.head.content.toString must include(msgs("site.change"))
      result(1).actions.value.items.head.content.toString   must include(msgs("site.change"))

      val expectedUrl =
        controllers.sa1.routes.EnterPreviousLastNameController
          .onPageLoad(userAnswersId, CheckMode)
          .url

      result.head.actions.value.items.head.href mustBe expectedUrl
      result(1).actions.value.items.head.href mustBe expectedUrl
    }
  }
}
