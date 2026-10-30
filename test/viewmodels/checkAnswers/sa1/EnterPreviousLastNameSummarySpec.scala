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
import pages.sa1.PreviousLastNamePage
import java.time.LocalDate
import play.api.test.Helpers.stubMessages

class EnterPreviousLastNameSummarySpec extends SpecBase {

  private val msgs = stubMessages()

  "EnterPreviousLastNameSummary" - {

    "must return no rows when previous name answers do not exist" in {
      val result =
        EnterPreviousLastNameSummary.rows(userAnswersId, emptyUserAnswers)(msgs)

      result mustBe empty
    }

    "must return two rows when previous name answers exist" in {
      val previousName = PreviousName(
        previousLastName = "John Doe",
        lastNameChangeDate = LocalDate.of(2020, 5, 10)
      )

      val userAnswers =
        emptyUserAnswers
          .set(PreviousLastNamePage, previousName)
          .success
          .value

      val result =
        EnterPreviousLastNameSummary.rows(userAnswersId, userAnswers)(msgs)

      result must have size 2

      result.head.value.content.toString must include("John Doe")
      result(1).value.content.toString   must include("10 May 2020")

      val expectedUrl =
        controllers.sa1.routes.EnterPreviousLastNameController
          .onPageLoad(userAnswersId, CheckMode)
          .url

      result.head.actions.value.items.head.href mustBe expectedUrl
      result(1).actions.value.items.head.href mustBe expectedUrl
    }
  }
}
