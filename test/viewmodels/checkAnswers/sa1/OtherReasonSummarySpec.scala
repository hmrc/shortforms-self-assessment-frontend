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
import models.sa1.OtherReason
import pages.sa1.OtherReasonPage
import play.api.test.Helpers.stubMessages

import java.time.LocalDate

class OtherReasonSummarySpec extends SpecBase {

  private val msgs = stubMessages()

  "OtherReasonSummary" - {

    "must return no rows when the other reason answer does not exist" in {
      val result = OtherReasonSummary.rows(userAnswersId, emptyUserAnswers)(msgs)

      result mustBe empty
    }

    "must return two rows when the other reason answer exists" in {
      val otherReason = OtherReason(
        reason = "Any reason",
        reasonDate = LocalDate.of(2020, 5, 10)
      )

      val userAnswers = emptyUserAnswers
        .set(OtherReasonPage, otherReason)
        .success
        .value

      val result = OtherReasonSummary.rows(userAnswersId, userAnswers)(msgs)

      result must have size 2

      result.head.value.content.toString                            must include("Any reason")
      result.head.key.content.toString                              must include(msgs("sa1.otherReason.reason.checkYourAnswersLabel"))
      result.head.actions.value.items.head.href mustBe controllers.sa1.routes.OtherReasonController
        .onPageLoad(userAnswersId, CheckMode)
        .url
      result.head.actions.value.items.head.content.toString         must include(msgs("site.change"))
      result.head.actions.value.items.head.visuallyHiddenText.value must include(
        msgs("sa1.otherReason.reason.change.hidden")
      )

      result(1).value.content.toString                            must include("10 May 2020")
      result(1).key.content.toString                              must include(msgs("sa1.otherReason.date.checkYourAnswersLabel"))
      result(1).actions.value.items.head.href mustBe controllers.sa1.routes.OtherReasonController
        .onPageLoad(userAnswersId, CheckMode)
        .url
      result(1).actions.value.items.head.content.toString         must include(msgs("site.change"))
      result(1).actions.value.items.head.visuallyHiddenText.value must include(
        msgs("sa1.otherReason.date.change.hidden")
      )
    }
  }
}
