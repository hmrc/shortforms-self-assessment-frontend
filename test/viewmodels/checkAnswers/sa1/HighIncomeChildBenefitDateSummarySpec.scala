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
import pages.sa1.HighIncomeChildBenefitDatePage
import play.api.test.Helpers.stubMessages

import java.time.LocalDate

class HighIncomeChildBenefitDateSummarySpec extends SpecBase {

  private val msgs = stubMessages()

  "HighIncomeChildBenefitDateSummary" - {

    "must return no rows when the high income child benefits date answer does not exist" in {
      val result = HighIncomeChildBenefitDateSummary.rows(userAnswersId, emptyUserAnswers)(msgs)

      result mustBe empty
    }

    "must return one row with the high income child benefits date answer exists" in {
      val userAnswers =
        emptyUserAnswers
          .set(HighIncomeChildBenefitDatePage, LocalDate.of(2022, 3, 19))
          .success
          .value

      val result = HighIncomeChildBenefitDateSummary.rows(userAnswersId, userAnswers)(msgs)
      result                             must have size 1
      result.head.value.content.toString must include("19 March 2022")
      result.head.key.content.toString   must include(msgs("sa1.highIncomeChildBenefitDate.checkYourAnswersLabel"))

      val expectedUrl =
        controllers.sa1.routes.HighIncomeChildBenefitDateController
          .onPageLoad(userAnswersId, CheckMode)
          .url
      result.head.actions.value.items.head.href mustBe expectedUrl
      result.head.actions.value.items.head.content.toString         must include(msgs("site.change"))
      result.head.actions.value.items.head.visuallyHiddenText.value must include(
        msgs("sa1.highIncomeChildBenefitDate.change.hidden")
      )
    }
  }
}
