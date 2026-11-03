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
import pages.sa1.{DoYouHaveANinoPage, NoNinoReasonPage}
import play.api.test.Helpers.stubMessages

class NoNinoReasonSummarySpec extends SpecBase {

  private val msgs = stubMessages()

  "NoNinoReasonSummary" - {

    "must return no row when the user has a NINO" in {
      val userAnswers =
        emptyUserAnswers
          .set(DoYouHaveANinoPage, true)
          .success
          .value

      val result = NoNinoReasonSummary.row(userAnswers)(msgs)

      result mustBe None
    }

    "must return no row when the NINO question has not been answered" in {
      val result = NoNinoReasonSummary.row(emptyUserAnswers)(msgs)

      result mustBe None
    }

    "must show not provided with an Add action when the reason is missing" in {
      val userAnswers =
        emptyUserAnswers
          .set(DoYouHaveANinoPage, false)
          .success
          .value

      val result = NoNinoReasonSummary.row(userAnswers)(msgs)

      result mustBe defined
      result.value.value.content.toString                    must include(msgs("site.notProvided"))
      result.value.actions.value.items.head.content.toString must include(msgs("site.add"))

      val expectedUrl =
        controllers.sa1.routes.NoNinoReasonController
          .onPageLoad(userAnswersId, CheckMode)
          .url

      result.value.actions.value.items.head.href mustBe expectedUrl
    }

    "must show the saved reason with a Change action when the reason exists" in {
      val reason = "I do not have a National Insurance number"

      val userAnswers =
        emptyUserAnswers
          .set(DoYouHaveANinoPage, false)
          .success
          .value
          .set(NoNinoReasonPage, reason)
          .success
          .value

      val result = NoNinoReasonSummary.row(userAnswers)(msgs)

      result mustBe defined
      result.value.value.content.toString                    must include(reason)
      result.value.actions.value.items.head.content.toString must include(msgs("site.change"))

      val expectedUrl =
        controllers.sa1.routes.NoNinoReasonController
          .onPageLoad(userAnswersId, CheckMode)
          .url

      result.value.actions.value.items.head.href mustBe expectedUrl
      result.value.actions.value.items.head.visuallyHiddenText.value mustBe
        msgs("sa1.noNinoReason.change.hidden")
    }
  }
}
