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
import pages.sa1.DateAnnualIncomePage
import play.api.test.Helpers.stubMessages

import java.time.LocalDate

class DateAnnualIncomeSummarySpec extends SpecBase {

  private val msgs = stubMessages()

  "DateAnnualIncomeSummary" - {

    "must return no rows when the annual income date answer does not exist" in {
      val result = DateAnnualIncomeSummary.rows(userAnswersId, emptyUserAnswers)(msgs)

      result mustBe empty
    }

    "must return one row with the annual income date answer exists" in {
      val userAnswers =
        emptyUserAnswers
          .set(DateAnnualIncomePage, LocalDate.of(2022, 3, 19))
          .success
          .value

      val result = DateAnnualIncomeSummary.rows(userAnswersId, userAnswers)(msgs)
      result                             must have size 1
      result.head.value.content.toString must include("19 March 2022")
      result.head.key.content.toString   must include(msgs("sa1.dateAnnualIncome.checkYourAnswersLabel"))

      val expectedUrl =
        controllers.sa1.routes.DateAnnualIncomeController
          .onPageLoad(userAnswersId, CheckMode)
          .url
      result.head.actions.value.items.head.href mustBe expectedUrl
      result.head.actions.value.items.head.content.toString         must include(msgs("site.change"))
      result.head.actions.value.items.head.visuallyHiddenText.value must include(
        msgs("sa1.dateAnnualIncome.change.hidden")
      )
    }
  }
}
