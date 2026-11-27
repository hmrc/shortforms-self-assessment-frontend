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
import pages.cwf1.SelfEmployedWorkPage
import play.api.test.Helpers.stubMessages

class SelfEmployedWorkSummarySpec extends SpecBase {

  private val msgs = stubMessages()

  "SelfEmployedWorkSummary" - {
    "must return no rows when name answers do not exist" in {
      val result = SelfEmployedWorkSummary.row(emptyUserAnswers)(msgs)

      result mustBe empty
    }

    "must return a row when name answers exist" in {
      val work = "Software Developer"

      val userAnswers =
        emptyUserAnswers
          .set(SelfEmployedWorkPage, work)
          .success
          .value

      val result = SelfEmployedWorkSummary.row(userAnswers)(msgs)

      result.value.key.content.toString   must include(msgs("cwf1.selfEmployedWork.checkYourAnswersLabel"))
      result.value.value.content.toString must include(work)

      val actionItem = result.value.actions.get.items

      actionItem.head.content.toString must include(msgs("site.change"))
      actionItem.head.visuallyHiddenText.value mustBe msgs("cwf1.selfEmployedWork.change.hidden")
      actionItem.head.attributes("id") mustBe "self-employed-work"

      val expectedUrl =
        controllers.cwf1.routes.SelfEmployedWorkController
          .onPageLoad(userAnswersId, CheckMode)
          .url

      actionItem.head.href mustBe expectedUrl
    }
  }
}
