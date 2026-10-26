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

package models.sa1

import models.{Enumerable, WithName}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.checkboxes.CheckboxItem
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import viewmodels.govuk.checkbox.*

sealed trait WhyTaxReturn

object WhyTaxReturn extends Enumerable.Implicits {

  case object CompanyDirector extends WithName("companyDirector") with WhyTaxReturn
  case object UkPropertyIncome extends WithName("ukPropertyIncome") with WhyTaxReturn
  case object ForeignIncome extends WithName("foreignIncome") with WhyTaxReturn
  case object TrustIncome extends WithName("trustIncome") with WhyTaxReturn
  case object IncomeThresholds extends WithName("incomeThresholds") with WhyTaxReturn
  case object UntaxedIncome extends WithName("untaxedIncome") with WhyTaxReturn
  case object ChildBenefitCharge extends WithName("childBenefitCharge") with WhyTaxReturn
  case object CapitalGainsTax extends WithName("capitalGainsTax") with WhyTaxReturn
  case object Other extends WithName("other") with WhyTaxReturn

  val values: Seq[WhyTaxReturn] = Seq(
    CompanyDirector,
    UkPropertyIncome,
    ForeignIncome,
    TrustIncome,
    IncomeThresholds,
    UntaxedIncome,
    ChildBenefitCharge,
    CapitalGainsTax,
    Other
  )

  def checkboxItems(implicit messages: Messages): Seq[CheckboxItem] =
    values.zipWithIndex.map { case (value, index) =>
      CheckboxItemViewModel(
        content = Text(messages(s"sa1.whyTaxReturn.$value")),
        fieldId = "value",
        index = index,
        value = value.toString
      )
    }

  implicit val enumerable: Enumerable[WhyTaxReturn] =
    Enumerable(values.map(v => v.toString -> v)*)
}
