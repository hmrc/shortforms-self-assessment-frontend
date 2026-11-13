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

package services.sa1

import models.UserAnswers
import models.sa1.WhyTaxReturn
import pages.AddressPage
import pages.sa1.*

import javax.inject.{Inject, Singleton}

@Singleton
class CheckYourAnswersValidator @Inject() () {
  def areRequiredDataPresent(ua: UserAnswers): Boolean =
    ua.get(NamePage).isDefined &&
      isPreviousLastNamePresent(ua) &&
      ua.get(DateOfBirthPage).isDefined &&
      ua.get(ContactDetailsPage).isDefined &&
      ua.get(AddressPage).isDefined &&
      ua.get(MoveAddressPage).isDefined &&
      isNinoOrNoNinoReasonPresent(ua) &&
      areTaxReturnReasonsPresent(ua)

  private def isPreviousLastNamePresent(ua: UserAnswers): Boolean =
    ua.get(AddPreviousLastNamePage) match {
      case Some(true)  => ua.get(PreviousLastNamePage).isDefined
      case Some(false) => true
      case None        => false
    }

  private def isNinoOrNoNinoReasonPresent(ua: UserAnswers): Boolean =
    ua.get(DoYouHaveANinoPage) match {
      case Some(true)  => ua.get(EnterNationalInsuranceNumberPage).isDefined
      case Some(false) => ua.get(NoNinoReasonPage).isDefined
      case None        => false
    }

  private def areTaxReturnReasonsPresent(ua: UserAnswers): Boolean =
    ua.get(WhyTaxReturnPage).exists { reasons =>
      reasons.forall {
        case WhyTaxReturn.CompanyDirector    => ua.get(DateUntaxedIncomeDirectorPage).isDefined
        case WhyTaxReturn.UkPropertyIncome   => ua.get(DateLandPropertyPage).isDefined
        case WhyTaxReturn.ForeignIncome      => ua.get(TaxableForeignIncomeDatePage).isDefined
        case WhyTaxReturn.TrustIncome        => ua.get(TrustSettlementDatePage).isDefined
        case WhyTaxReturn.IncomeThresholds   => ua.get(DateAnnualIncomePage).isDefined
        case WhyTaxReturn.UntaxedIncome      => ua.get(UntaxedIncomeNotInPayeStartDatePage).isDefined
        case WhyTaxReturn.ChildBenefitCharge => ua.get(HighIncomeChildBenefitDatePage).isDefined
        case WhyTaxReturn.CapitalGainsTax    => ua.get(DateCapitalGainsTaxPage).isDefined
        case WhyTaxReturn.Other              => ua.get(OtherReasonPage).map(_.reasonDate).isDefined
      }
    }
}
