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
import models.sa1.{OtherReason, WhyTaxReturn}
import pages.sa1.{DateUntaxedIncomeDirectorPage, OtherReasonPage, WhyTaxReturnPage}
import play.api.i18n.Messages
import play.api.test.Helpers.{stubMessages, stubMessagesApi}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent

import java.time.LocalDate

class WhyTaxReturnSummarySpec extends SpecBase {

  private implicit val msgs: Messages = stubMessages()

  "WhyTaxReturnSummary" - {

    "must return no rows when the answer is not present" in {
      val result = WhyTaxReturnSummary.rows(emptyUserAnswers)

      result mustBe empty
    }

    "must return reason and date rows in display order with correct links" in {
      val reasons: Set[WhyTaxReturn] =
        Set(WhyTaxReturn.Other, WhyTaxReturn.CompanyDirector, WhyTaxReturn.CapitalGainsTax)

      val otherReason = OtherReason(
        "Additional income details",
        LocalDate.of(2020, 5, 10)
      )

      val userAnswers =
        emptyUserAnswers
          .set(WhyTaxReturnPage, reasons)
          .success
          .value
          .set(OtherReasonPage, otherReason)
          .success
          .value

      val result = WhyTaxReturnSummary.rows(userAnswers)

      result must have size 6

      result(0).value.content.asHtml.toString must include(msgs("sa1.whyTaxReturn.companyDirector"))
      result(1).value.content.asHtml.toString must include(msgs("site.notProvided"))

      result(2).value.content.asHtml.toString must include(msgs("sa1.whyTaxReturn.capitalGainsTax"))
      result(3).value.content.asHtml.toString must include(msgs("site.notProvided"))

      result(4).value.content.asHtml.toString must include("Additional income details")
      result(5).value.content.asHtml.toString must include("10 May 2020")

      val reasonUrl =
        controllers.sa1.routes.WhyTaxReturnController.onPageLoad(userAnswersId, CheckMode).url

      result(0).actions.value.items.head.href mustBe reasonUrl
      result(2).actions.value.items.head.href mustBe reasonUrl
      result(4).actions.value.items.head.href mustBe reasonUrl

      result(0).actions.value.items.head.content.asHtml.toString must include(msgs("site.change"))

      val dateUrls = Seq(
        controllers.sa1.routes.DateUntaxedIncomeDirectorController.onPageLoad(userAnswersId, CheckMode).url,
        controllers.sa1.routes.DateCapitalGainsTaxController.onPageLoad(userAnswersId, CheckMode).url,
        controllers.sa1.routes.OtherReasonController.onPageLoad(userAnswersId, CheckMode).url
      )

      Seq(1, 3, 5).zip(dateUrls).foreach { case (index, expectedUrl) =>
        result(index).actions.value.items.head.href mustBe expectedUrl

        val expectedAction =
          if (index == 5) msgs("site.change") else msgs("site.add")

        result(index).actions.value.items.head.content.asHtml.toString must include(expectedAction)
      }
    }

    "must display the saved date with a Change action" in {
      val date = LocalDate.of(2020, 5, 10)

      val userAnswers =
        emptyUserAnswers
          .set(WhyTaxReturnPage, Set(WhyTaxReturn.CompanyDirector))
          .success
          .value
          .set(DateUntaxedIncomeDirectorPage, date)
          .success
          .value

      val result = WhyTaxReturnSummary.rows(userAnswers)

      result must have size 2

      result(1).value.content.asHtml.toString                    must include("10 May 2020")
      result(1).actions.value.items.head.content.asHtml.toString must include(msgs("site.change"))

      result(1).actions.value.items.head.href mustBe
        controllers.sa1.routes.DateUntaxedIncomeDirectorController
          .onPageLoad(userAnswersId, CheckMode)
          .url
    }

    "must HTML-escape the translated reason text" in {
      val escapingMsgs: Messages = stubMessages(
        stubMessagesApi(
          Map("default" -> Map("sa1.whyTaxReturn.untaxedIncome" -> "<b>wasn''t</b> & \"quoted\""))
        )
      )

      val userAnswers =
        emptyUserAnswers
          .set(WhyTaxReturnPage, Set[WhyTaxReturn](WhyTaxReturn.UntaxedIncome))
          .success
          .value

      val result = WhyTaxReturnSummary.rows(userAnswers)(escapingMsgs)

      result.head.value.content.asInstanceOf[HtmlContent].asHtml.toString mustEqual
        "&lt;b&gt;wasn&#x27;t&lt;/b&gt; &amp; &quot;quoted&quot;"
    }

    "must HTML-escape the user-entered Other reason" in {
      val otherReason = OtherReason(
        "<b>wasn't</b> & \"quoted\"",
        LocalDate.of(2020, 5, 10)
      )

      val userAnswers =
        emptyUserAnswers
          .set(WhyTaxReturnPage, Set[WhyTaxReturn](WhyTaxReturn.Other))
          .success
          .value
          .set(OtherReasonPage, otherReason)
          .success
          .value

      val result = WhyTaxReturnSummary.rows(userAnswers)

      result.head.value.content.asInstanceOf[HtmlContent].asHtml.toString mustEqual
        "&lt;b&gt;wasn&#x27;t&lt;/b&gt; &amp; &quot;quoted&quot;"
    }
  }
}
