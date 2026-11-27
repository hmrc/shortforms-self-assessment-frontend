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
import pages.cwf1.AreYouUkResidentPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text

class AreYouUkResidentSummarySpec extends SpecBase {

  private given Messages = play.api.test.Helpers.stubMessages()

  "AreYouUkResidentSummary" - {
    "when the answer is present" - {
      "must return a SummaryListRow with 'yes' text and change link" in {
        val userAnswers = emptyUserAnswers.set(AreYouUkResidentPage, true).success.value

        val row = AreYouUkResidentSummary.row(userAnswers).value

        row.key.content.asHtml.toString() mustEqual "cwf1.areYouUkResident.checkYourAnswersLabel"

        val contentString = row.value.content.asInstanceOf[Text].asHtml.toString()
        contentString mustEqual "site.yes"

        val actionItem = row.actions.get.items.loneElement
        actionItem.href mustEqual controllers.cwf1.routes.AreYouUkResidentController
          .onPageLoad(userAnswersId, CheckMode)
          .url

        actionItem.content.asHtml.toString() must include("site.change")
        actionItem.visuallyHiddenText.value mustEqual "cwf1.areYouUkResident.change.hidden"
        actionItem.attributes.get("id") mustBe Some("uk-resident")
      }

      "must return a SummaryListRow with 'no' text and change link" in {

        val userAnswers = emptyUserAnswers.set(AreYouUkResidentPage, false).success.value

        val row = AreYouUkResidentSummary.row(userAnswers).value

        row.key.content.asHtml.toString() mustEqual "cwf1.areYouUkResident.checkYourAnswersLabel"

        val contentString = row.value.content.asInstanceOf[Text].asHtml.toString()
        contentString mustEqual "site.no"

        val actionItem = row.actions.get.items.loneElement
        actionItem.href mustEqual controllers.cwf1.routes.AreYouUkResidentController
          .onPageLoad(userAnswersId, CheckMode)
          .url

        actionItem.content.asHtml.toString() must include("site.change")
        actionItem.visuallyHiddenText.value mustEqual "cwf1.areYouUkResident.change.hidden"
        actionItem.attributes.get("id") mustBe Some("uk-resident")
      }
    }
  }

  "when the answer is not present" - {
    "must return None" in {
      AreYouUkResidentSummary.row(emptyUserAnswers).isEmpty mustEqual true
    }
  }

}
