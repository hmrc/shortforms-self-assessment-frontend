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
import org.scalatest.LoneElement
import pages.EnterNiNumberPage
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages

class EnterNiNumberSummarySpec extends SpecBase with LoneElement {
  private given Messages = stubMessages()

  private val givenNiNumber = "NM772208B"

  "EnterNationalInsuranceNumberSummary must" - {

    "include a single change action when" - {

      "user answers contain NI number" in {
        val userAnswers = emptyUserAnswers
          .set(EnterNiNumberPage, givenNiNumber)
          .success
          .value

        val expectedUrl = controllers.cwf1.routes.EnterNiNumberController.onPageLoad(userAnswersId, CheckMode).url

        val result = EnterNiNumberSummary.row(userAnswers)
        result.value.content.asHtml.toString mustBe givenNiNumber

        val action = result.actions.value.items.loneElement
        action.href mustBe expectedUrl
        action.content.asHtml.toString mustBe "site.change"
      }
    }

    "include a single add action when" - {

      "user answers do NOT contain NI number" in {
        val userAnswers = emptyUserAnswers

        val expectedUrl = controllers.cwf1.routes.EnterNiNumberController.onPageLoad(userAnswersId, CheckMode).url

        val result = EnterNiNumberSummary.row(userAnswers)
        result.value.content.asHtml.toString mustBe "site.notProvided"

        val action = result.actions.value.items.loneElement
        action.href mustBe expectedUrl
        action.content.asHtml.toString mustBe "site.add"
      }
    }
  }
}
