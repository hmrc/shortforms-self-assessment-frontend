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

package viewmodels.checkAnswers.cwf1

import base.SpecBase
import models.CheckMode
import org.scalatest.LoneElement.convertToCollectionLoneElementWrapper
import pages.cwf1.SelfEmploymentStartDatePage
import play.api.test.Helpers.stubMessages

import java.time.LocalDate

class SelfEmploymentStartDateSummarySpec extends SpecBase {

  private val msgs = stubMessages()

  "SelfEmploymentStartDateSummary" - {

    "must return no rows when the selfEmploymentStartDate answer does not exist" in {
      val result = SelfEmploymentStartDateSummary.rows(emptyUserAnswers)(msgs)

      result mustBe empty
    }

    "must return one row when the selfEmploymentStartDate answer exists" in {
      val selfEmploymentStartDate = LocalDate.of(1990, 5, 10)

      val userAnswers = emptyUserAnswers
        .set(SelfEmploymentStartDatePage, selfEmploymentStartDate)
        .success
        .value

      val result = SelfEmploymentStartDateSummary.rows(userAnswers)(msgs)

      result                                      must have size 1
      result.head.key.content.asHtml.toString()   must include(msgs("cwf1.selfEmploymentStartDate.checkYourAnswersLabel"))
      result.head.value.content.asHtml.toString() must include("10 May 1990")

      val actionItem = result.head.actions.value.items.loneElement

      actionItem.href mustBe controllers.cwf1.routes.SelfEmploymentStartDateController
        .onPageLoad(userAnswersId, CheckMode)
        .url
      actionItem.content.asHtml.toString() must include(msgs("site.change"))
      actionItem.visuallyHiddenText.value  must include(
        msgs("cwf1.selfEmploymentStartDate.change.hidden")
      )
      actionItem.attributes("id")          must include("self-employment-start")
    }
  }
}
