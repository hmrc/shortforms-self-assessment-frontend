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
import pages.sa1.UntaxedIncomeNotInPayeStartDatePage
import play.api.i18n.Messages

import java.time.LocalDate

class UntaxedIncomeNotInPayeStartDateSummarySpec extends SpecBase {
  private given Messages = play.api.test.Helpers.stubMessages()

  "UntaxedIncomeNotInPayeStartDateSummary" - {

    "when untaxed income not in PAYE start date is present" - {

      "must return a row with the date and change link" in {
        val userAnswers = emptyUserAnswers
          .set(UntaxedIncomeNotInPayeStartDatePage, LocalDate.of(2000, 10, 26))
          .success
          .value

        val result = UntaxedIncomeNotInPayeStartDateSummary.row(userAnswers).value

        result.key.content.asHtml.toString() mustEqual "sa1.untaxedIncomeNotInPayeStartDate.checkYourAnswersLabel"

        result.value.content.asHtml.toString() mustEqual "26 October 2000"
        result.actions.get.items.size mustEqual 1
        result.actions.get.items.head.href mustEqual controllers.sa1.routes.UntaxedIncomeNotInPayeStartDateController
          .onPageLoad(userAnswers.id, CheckMode)
          .url
        result.actions.get.items.head.content.asHtml.toString() must include("site.change")
        result.actions.get.items.head.visuallyHiddenText.value mustEqual "sa1.untaxedIncomeNotInPayeStartDate.change.hidden"
      }
    }

    "when untaxed income not in PAYE start date is not present" - {

      "must return None" in {
        UntaxedIncomeNotInPayeStartDateSummary.row(emptyUserAnswers).isEmpty mustEqual true
      }
    }

  }
}
