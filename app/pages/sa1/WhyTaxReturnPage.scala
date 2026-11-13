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

package pages.sa1

import models.UserAnswers
import models.sa1.WhyTaxReturn
import pages.QuestionPage
import play.api.libs.json.JsPath
import scala.util.Try

case object WhyTaxReturnPage extends QuestionPage[Set[WhyTaxReturn]] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "whyTaxReturn"

  def cleanupRemovedAnswers(
    previousAnswers: Set[WhyTaxReturn],
    newAnswers: Set[WhyTaxReturn],
    userAnswers: UserAnswers
  ): Try[UserAnswers] = {

    val removedAnswers = previousAnswers.diff(newAnswers)

    removedAnswers.foldLeft(Try(userAnswers)) {
      case (acc, WhyTaxReturn.CompanyDirector) =>
        acc.flatMap(_.remove(DateUntaxedIncomeDirectorPage))

      case (acc, WhyTaxReturn.UkPropertyIncome) =>
        acc.flatMap(_.remove(DateLandPropertyPage))

      case (acc, WhyTaxReturn.ForeignIncome) =>
        acc.flatMap(_.remove(TaxableForeignIncomeDatePage))

      case (acc, WhyTaxReturn.TrustIncome) =>
        acc.flatMap(_.remove(TrustSettlementDatePage))

      case (acc, WhyTaxReturn.IncomeThresholds) =>
        acc.flatMap(_.remove(DateAnnualIncomePage))

      case (acc, WhyTaxReturn.UntaxedIncome) =>
        acc.flatMap(_.remove(UntaxedIncomeNotInPayeStartDatePage))

      case (acc, WhyTaxReturn.ChildBenefitCharge) =>
        acc.flatMap(_.remove(HighIncomeChildBenefitDatePage))

      case (acc, WhyTaxReturn.CapitalGainsTax) =>
        acc.flatMap(_.remove(DateCapitalGainsTaxPage))

      case (acc, WhyTaxReturn.Other) =>
        acc.flatMap(_.remove(OtherReasonPage))
    }
  }
}
