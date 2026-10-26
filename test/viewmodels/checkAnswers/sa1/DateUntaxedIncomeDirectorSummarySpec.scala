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
import pages.sa1.DateUntaxedIncomeDirectorPage
import play.api.test.Helpers.stubMessages

import java.time.LocalDate

class DateUntaxedIncomeDirectorSummarySpec extends SpecBase {

  private val msgs = stubMessages()

  "DateUntaxedIncomeDirectorSummary" - {

    "must return no rows when the date untaxed income director answer does not exist" in {
      val result = DateUntaxedIncomeDirectorSummary.rows(userAnswersId, emptyUserAnswers)(msgs)

      result mustBe empty
    }

    "must return one row when the date untaxed income director answer exists" in {
      val dateUntaxedIncomeDirector = LocalDate.of(1990, 5, 10)

      val userAnswers = emptyUserAnswers
        .set(DateUntaxedIncomeDirectorPage, dateUntaxedIncomeDirector)
        .success
        .value

      val result = DateUntaxedIncomeDirectorSummary.rows(userAnswersId, userAnswers)(msgs)

      result                                                        must have size 1
      result.head.value.content.toString                            must include("10 May 1990")
      result.head.key.content.toString                              must include(msgs("sa1.dateUntaxedIncomeDirector.checkYourAnswersLabel"))
      result.head.actions.value.items.head.href mustBe controllers.sa1.routes.DateUntaxedIncomeDirectorController
        .onPageLoad(userAnswersId, CheckMode)
        .url
      result.head.actions.value.items.head.content.toString         must include(msgs("site.change"))
      result.head.actions.value.items.head.visuallyHiddenText.value must include(
        msgs("sa1.dateUntaxedIncomeDirector.change.hidden")
      )
    }
  }
}
