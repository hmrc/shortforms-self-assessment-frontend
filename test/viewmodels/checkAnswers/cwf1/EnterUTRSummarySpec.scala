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
import pages.cwf1.EnterUTRPage
import play.api.test.Helpers.stubMessages

class EnterUTRSummarySpec extends SpecBase {

  private val msgs = stubMessages()

  "EnterUTRSummary" - {

    "must return no row when the UTR has not been provided" in {
      val result =
        EnterUTRSummary.row(emptyUserAnswers)(msgs)

      result mustBe empty
    }

    "must return a populated row when the UTR answer exists" in {
      val utr = "1234567890"

      val userAnswers =
        emptyUserAnswers
          .set(EnterUTRPage, utr)
          .success
          .value

      val result =
        EnterUTRSummary.row(userAnswers)(msgs)

      result.value.key.content.toString   must include(msgs("cwf1.enterUTR.checkYourAnswersLabel"))
      result.value.value.content.toString must include(utr)

      val actionItem = result.value.actions.get.items

      actionItem.head.content.toString must include(msgs("site.change"))
      actionItem.head.visuallyHiddenText.value mustBe msgs("cwf1.enterUTR.change.hidden")
      actionItem.head.attributes("id") mustBe "unique-taxpayer-reference"

      val expectedUrl =
        controllers.cwf1.routes.EnterUTRController
          .onPageLoad(userAnswersId, CheckMode)
          .url

      actionItem.head.href mustBe expectedUrl
    }
  }
}
