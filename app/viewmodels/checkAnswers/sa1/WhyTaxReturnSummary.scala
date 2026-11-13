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

import models.sa1.WhyTaxReturn
import models.{CheckMode, UserAnswers}
import pages.sa1.*
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

import java.time.format.DateTimeFormatter
import java.util.Locale

object WhyTaxReturnSummary {

  private val dateFormatter =
    DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.UK)

  def rows(answers: UserAnswers)(implicit messages: Messages): Seq[SummaryListRow] =
    answers
      .get(WhyTaxReturnPage)
      .toSeq
      .flatMap(selected => WhyTaxReturn.values.filter(selected.contains))
      .flatMap { reason =>
        createReasonRow(reason, answers) +: createDateRow(reason, answers).toSeq
      }

  private def createReasonRow(
    reason: WhyTaxReturn,
    answers: UserAnswers
  )(implicit messages: Messages): SummaryListRow = {

    val reasonText = reason match {
      case WhyTaxReturn.Other =>
        answers.get(OtherReasonPage).map(_.reason).getOrElse(messages(s"sa1.whyTaxReturn.$reason"))
      case _                  =>
        messages(s"sa1.whyTaxReturn.$reason")
    }

    val value = HtmlFormat.escape(reasonText).toString

    SummaryListRowViewModel(
      key = "sa1.whyTaxReturn.reason.checkYourAnswersLabel",
      value = ValueViewModel(HtmlContent(value)),
      actions = Seq(
        ActionItemViewModel(
          "site.change",
          controllers.sa1.routes.WhyTaxReturnController
            .onPageLoad(answers.id, CheckMode)
            .url
        ).withVisuallyHiddenText(messages("sa1.whyTaxReturn.reason.change.hidden"))
          .withAttribute("id" -> reasonChangeId(reason))
      )
    )
  }

  private def createDateRow(
    reason: WhyTaxReturn,
    answers: UserAnswers
  )(implicit messages: Messages): Option[SummaryListRow] = {

    val date = reason match {
      case WhyTaxReturn.CompanyDirector    => answers.get(DateUntaxedIncomeDirectorPage)
      case WhyTaxReturn.UkPropertyIncome   => answers.get(DateLandPropertyPage)
      case WhyTaxReturn.ForeignIncome      => answers.get(TaxableForeignIncomeDatePage)
      case WhyTaxReturn.TrustIncome        => answers.get(TrustSettlementDatePage)
      case WhyTaxReturn.IncomeThresholds   => answers.get(DateAnnualIncomePage)
      case WhyTaxReturn.UntaxedIncome      => answers.get(UntaxedIncomeNotInPayeStartDatePage)
      case WhyTaxReturn.ChildBenefitCharge => answers.get(HighIncomeChildBenefitDatePage)
      case WhyTaxReturn.CapitalGainsTax    => answers.get(DateCapitalGainsTaxPage)
      case WhyTaxReturn.Other              => answers.get(OtherReasonPage).map(_.reasonDate)
    }

    val value = date.fold(HtmlContent(messages("site.notProvided"))) { startDate =>
      HtmlContent(HtmlFormat.escape(startDate.format(dateFormatter)).toString)
    }

    val action = ActionItemViewModel(date.fold("site.add")(_ => "site.change"), reasonRoute(reason, answers.id).url)
      .withVisuallyHiddenText(messages("sa1.whyTaxReturn.startDate.change.hidden"))
      .withAttribute("id" -> reasonDateChangeId(reason))

    Some(
      SummaryListRowViewModel(
        key = "sa1.whyTaxReturn.startDate.checkYourAnswersLabel",
        value = ValueViewModel(value),
        actions = Seq(action)
      )
    )
  }

  private def reasonRoute(reason: WhyTaxReturn, journeyId: String) =
    reason match {
      case WhyTaxReturn.CompanyDirector    =>
        controllers.sa1.routes.DateUntaxedIncomeDirectorController.onPageLoad(journeyId, CheckMode)
      case WhyTaxReturn.UkPropertyIncome   =>
        controllers.sa1.routes.DateLandPropertyController.onPageLoad(journeyId, CheckMode)
      case WhyTaxReturn.ForeignIncome      =>
        controllers.sa1.routes.TaxableForeignIncomeDateController.onPageLoad(journeyId, CheckMode)
      case WhyTaxReturn.TrustIncome        =>
        controllers.sa1.routes.TrustSettlementDateController.onPageLoad(journeyId, CheckMode)
      case WhyTaxReturn.IncomeThresholds   =>
        controllers.sa1.routes.DateAnnualIncomeController.onPageLoad(journeyId, CheckMode)
      case WhyTaxReturn.UntaxedIncome      =>
        controllers.sa1.routes.UntaxedIncomeNotInPayeStartDateController.onPageLoad(journeyId, CheckMode)
      case WhyTaxReturn.ChildBenefitCharge =>
        controllers.sa1.routes.HighIncomeChildBenefitDateController.onPageLoad(journeyId, CheckMode)
      case WhyTaxReturn.CapitalGainsTax    =>
        controllers.sa1.routes.DateCapitalGainsTaxController.onPageLoad(journeyId, CheckMode)
      case WhyTaxReturn.Other              =>
        controllers.sa1.routes.OtherReasonController.onPageLoad(journeyId, CheckMode)
    }

  private def reasonDateChangeId(reason: WhyTaxReturn): String =
    reasonChangeId(reason) + "-date"

  private def reasonChangeId(reason: WhyTaxReturn): String =
    reason match {
      case WhyTaxReturn.CompanyDirector    => "company-director-untaxed"
      case WhyTaxReturn.UkPropertyIncome   => "income-land-property"
      case WhyTaxReturn.ForeignIncome      => "taxable-foreign-income-dividends"
      case WhyTaxReturn.TrustIncome        => "annual-income-trust-settlement"
      case WhyTaxReturn.IncomeThresholds   => "income-exceeds-annual-threshold"
      case WhyTaxReturn.UntaxedIncome      => "untaxed-income-paye-tax-code"
      case WhyTaxReturn.ChildBenefitCharge => "high-income-child-benefit-charge"
      case WhyTaxReturn.CapitalGainsTax    => "capital-gains-tax"
      case WhyTaxReturn.Other              => "other-reason"
    }

}
