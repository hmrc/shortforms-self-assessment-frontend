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
import models.sa1.WhyTaxReturn
import pages.sa1.WhyTaxReturnPage
import play.api.i18n.Messages
import play.api.test.Helpers.running
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent

class WhyTaxReturnSummarySpec extends SpecBase {

  "WhyTaxReturnSummary" - {

    "when the answer is present" - {

      "must return a SummaryListRow listing the selected reasons in display order with a change link" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)
          val reasons: Set[WhyTaxReturn] =
            Set(WhyTaxReturn.Other, WhyTaxReturn.CompanyDirector, WhyTaxReturn.CapitalGainsTax)

          val userAnswers = emptyUserAnswers.set(WhyTaxReturnPage, reasons).success.value

          val row = WhyTaxReturnSummary.row(userAnswers).value

          row.key.content.asHtml.toString() mustEqual msgs("sa1.whyTaxReturn.checkYourAnswersLabel")

          row.value.content.asInstanceOf[HtmlContent].asHtml.toString() mustEqual Seq(
            msgs("sa1.whyTaxReturn.companyDirector"),
            msgs("sa1.whyTaxReturn.capitalGainsTax"),
            msgs("sa1.whyTaxReturn.other")
          ).mkString("<br>")

          row.actions.get.items.size mustEqual 1
          row.actions.get.items.head.href mustEqual controllers.sa1.routes.WhyTaxReturnController
            .onPageLoad(userAnswersId, CheckMode)
            .url
          row.actions.get.items.head.content.asHtml.toString() must include(msgs("site.change"))
          row.actions.get.items.head.visuallyHiddenText.value mustEqual msgs("sa1.whyTaxReturn.change.hidden")
        }
      }

      "must HTML-escape the reason text" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val userAnswers = emptyUserAnswers.set(WhyTaxReturnPage, Set[WhyTaxReturn](WhyTaxReturn.UntaxedIncome)).success.value

          val row = WhyTaxReturnSummary.row(userAnswers).value

          row.value.content.asInstanceOf[HtmlContent].asHtml.toString() mustEqual
            "I have untaxed income that wasn&#x27;t collected through my PAYE tax code"
        }
      }
    }

    "when the answer is not present" - {

      "must return None" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          WhyTaxReturnSummary.row(emptyUserAnswers) mustBe None
        }
      }
    }
  }
}
