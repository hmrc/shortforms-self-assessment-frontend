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

package viewmodels.govuk.checkAnswers.sa1

import base.SpecBase
import models.CheckMode
import pages.sa1.AddNiNumberPage
import play.api.i18n.Messages
import play.api.test.Helpers.running
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import viewmodels.checkAnswers.sa1.AddNiNumberSummary

class AddNiNumberSummarySpec extends SpecBase {

  "AddNiNumberSummary" - {
    "when the answer is present" - {
      "must return a SummaryListRow with 'yes' text and change link" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val userAnswers = emptyUserAnswers.set(AddNiNumberPage, true).success.value

          val row = AddNiNumberSummary.row(userAnswers).value

          row.key.content.asHtml.toString() mustEqual msgs("sa1.addNiNumber.checkYourAnswersLabel")

          val contentString = row.value.content.asInstanceOf[Text].asHtml.toString()
          contentString mustEqual msgs("site.yes")

          row.actions.get.items.size mustEqual 1
          row.actions.get.items.head.href mustEqual controllers.sa1.routes.AddNiNumberController
            .onPageLoad(userAnswersId, CheckMode)
            .url
          row.actions.get.items.head.content.asHtml.toString() must include(msgs("site.change"))
          row.actions.get.items.head.visuallyHiddenText.value mustEqual msgs("sa1.addNiNumber.change.hidden")
        }
      }

      "must return a SummaryListRow with 'no' text and change link" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val userAnswers = emptyUserAnswers.set(AddNiNumberPage, false).success.value

          val row = AddNiNumberSummary.row(userAnswers).value

          row.key.content.asHtml.toString() mustEqual msgs("sa1.addNiNumber.checkYourAnswersLabel")

          val contentString = row.value.content.asInstanceOf[Text].asHtml.toString()
          contentString mustEqual msgs("site.no")

          row.actions.get.items.size mustEqual 1
          row.actions.get.items.head.href mustEqual controllers.sa1.routes.AddNiNumberController
            .onPageLoad(userAnswersId, CheckMode)
            .url
          row.actions.get.items.head.content.asHtml.toString() must include(msgs("site.change"))
          row.actions.get.items.head.visuallyHiddenText.value mustEqual msgs("sa1.addNiNumber.change.hidden")
        }
      }
    }

    "when the answer is not present" - {
      "must return None" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          AddNiNumberSummary.row(emptyUserAnswers).isEmpty mustEqual true
        }
      }
    }
  }
}
