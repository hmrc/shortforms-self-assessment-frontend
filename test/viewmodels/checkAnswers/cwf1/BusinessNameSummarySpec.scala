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
import pages.cwf1.BusinessNamePage
import play.api.i18n.Messages

class BusinessNameSummarySpec extends SpecBase with LoneElement {
  private given Messages = play.api.test.Helpers.stubMessages()

  "NameSummary must" - {

    "return no rows when answer does NOT exist" in {
      val result = BusinessNameSummary.rows(emptyUserAnswers)

      result mustBe empty
    }

    "return one row when answer exists" in {
      val businessName = "ACME Inc"

      val userAnswers = emptyUserAnswers.update(BusinessNamePage, businessName)

      val result = BusinessNameSummary.rows(userAnswers).loneElement
      result.value.content.asHtml.toString mustBe "ACME Inc"

      val expectedUrl = controllers.cwf1.routes.BusinessNameController.onPageLoad(userAnswersId, CheckMode).url

      val actionItem = result.actions.value.items.loneElement
      actionItem.href mustBe expectedUrl
      actionItem.visuallyHiddenText.value mustBe "cwf1.businessName.change.hidden"
      actionItem.attributes("id") mustBe "business-name"
    }

    "escape the name to prevent HTML injection" in {
      val businessName = "<b>ACME Inc</b>"

      val userAnswers = emptyUserAnswers.update(BusinessNamePage, businessName)

      val result = BusinessNameSummary.rows(userAnswers).loneElement

      result.value.content.asHtml.toString mustBe "&lt;b&gt;ACME Inc&lt;/b&gt;"
    }
  }
}
