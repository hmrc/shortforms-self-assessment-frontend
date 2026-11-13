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

import base.SpecBase
import models.sa1.{OtherReason, WhyTaxReturn}

import java.time.LocalDate

class WhyTaxReturnPageSpec extends SpecBase {

  "WhyTaxReturnPage" - {

    "cleanupRemovedAnswers" - {

      "must remove DateUntaxedIncomeDirectorPage when CompanyDirector is deselected" in {

        val userAnswers = emptyUserAnswers
          .set(DateUntaxedIncomeDirectorPage, LocalDate.of(2025, 1, 1))
          .success
          .value

        val updatedAnswers = WhyTaxReturnPage
          .cleanupRemovedAnswers(
            Set(WhyTaxReturn.CompanyDirector),
            Set.empty,
            userAnswers
          )
          .success
          .value

        updatedAnswers.get(DateUntaxedIncomeDirectorPage) mustBe None
      }

      "must remove DateLandPropertyPage when UkPropertyIncome is deselected" in {

        val userAnswers = emptyUserAnswers
          .set(DateLandPropertyPage, LocalDate.of(2025, 1, 1))
          .success
          .value

        val updatedAnswers = WhyTaxReturnPage
          .cleanupRemovedAnswers(
            Set(WhyTaxReturn.UkPropertyIncome),
            Set.empty,
            userAnswers
          )
          .success
          .value

        updatedAnswers.get(DateLandPropertyPage) mustBe None
      }

      "must remove TaxableForeignIncomeDatePage when ForeignIncome is deselected" in {

        val userAnswers = emptyUserAnswers
          .set(TaxableForeignIncomeDatePage, LocalDate.of(2025, 1, 1))
          .success
          .value

        val updatedAnswers = WhyTaxReturnPage
          .cleanupRemovedAnswers(
            Set(WhyTaxReturn.ForeignIncome),
            Set.empty,
            userAnswers
          )
          .success
          .value

        updatedAnswers.get(TaxableForeignIncomeDatePage) mustBe None
      }

      "must remove TrustSettlementDatePage when TrustIncome is deselected" in {

        val userAnswers = emptyUserAnswers
          .set(TrustSettlementDatePage, LocalDate.of(2025, 1, 1))
          .success
          .value

        val updatedAnswers = WhyTaxReturnPage
          .cleanupRemovedAnswers(
            Set(WhyTaxReturn.TrustIncome),
            Set.empty,
            userAnswers
          )
          .success
          .value

        updatedAnswers.get(TrustSettlementDatePage) mustBe None
      }

      "must remove DateAnnualIncomePage when IncomeThresholds is deselected" in {

        val userAnswers = emptyUserAnswers
          .set(DateAnnualIncomePage, LocalDate.of(2025, 1, 1))
          .success
          .value

        val updatedAnswers = WhyTaxReturnPage
          .cleanupRemovedAnswers(
            Set(WhyTaxReturn.IncomeThresholds),
            Set.empty,
            userAnswers
          )
          .success
          .value

        updatedAnswers.get(DateAnnualIncomePage) mustBe None
      }

      "must remove UntaxedIncomeNotInPayeStartDatePage when UntaxedIncome is deselected" in {

        val userAnswers = emptyUserAnswers
          .set(UntaxedIncomeNotInPayeStartDatePage, LocalDate.of(2025, 1, 1))
          .success
          .value

        val updatedAnswers = WhyTaxReturnPage
          .cleanupRemovedAnswers(
            Set(WhyTaxReturn.UntaxedIncome),
            Set.empty,
            userAnswers
          )
          .success
          .value

        updatedAnswers.get(UntaxedIncomeNotInPayeStartDatePage) mustBe None
      }

      "must remove HighIncomeChildBenefitDatePage when ChildBenefitCharge is deselected" in {

        val userAnswers = emptyUserAnswers
          .set(HighIncomeChildBenefitDatePage, LocalDate.of(2025, 1, 1))
          .success
          .value

        val updatedAnswers = WhyTaxReturnPage
          .cleanupRemovedAnswers(
            Set(WhyTaxReturn.ChildBenefitCharge),
            Set.empty,
            userAnswers
          )
          .success
          .value

        updatedAnswers.get(HighIncomeChildBenefitDatePage) mustBe None
      }

      "must remove DateCapitalGainsTaxPage when CapitalGainsTax is deselected" in {

        val userAnswers = emptyUserAnswers
          .set(DateCapitalGainsTaxPage, LocalDate.of(2025, 1, 1))
          .success
          .value

        val updatedAnswers = WhyTaxReturnPage
          .cleanupRemovedAnswers(
            Set(WhyTaxReturn.CapitalGainsTax),
            Set.empty,
            userAnswers
          )
          .success
          .value

        updatedAnswers.get(DateCapitalGainsTaxPage) mustBe None
      }

      "must remove OtherReasonPage when Other is deselected" in {

        val otherReason = OtherReason(
          reason = "Additional income",
          reasonDate = LocalDate.of(2025, 1, 1)
        )

        val userAnswers = emptyUserAnswers
          .set(OtherReasonPage, otherReason)
          .success
          .value

        val updatedAnswers = WhyTaxReturnPage
          .cleanupRemovedAnswers(
            Set(WhyTaxReturn.Other),
            Set.empty,
            userAnswers
          )
          .success
          .value

        updatedAnswers.get(OtherReasonPage) mustBe None
      }

      "must not remove dependent answers when a reason remains selected" in {

        val userAnswers = emptyUserAnswers
          .set(DateUntaxedIncomeDirectorPage, LocalDate.of(2025, 1, 1))
          .success
          .value

        val updatedAnswers = WhyTaxReturnPage
          .cleanupRemovedAnswers(
            Set(WhyTaxReturn.CompanyDirector),
            Set(WhyTaxReturn.CompanyDirector),
            userAnswers
          )
          .success
          .value

        updatedAnswers.get(DateUntaxedIncomeDirectorPage) mustBe Some(LocalDate.of(2025, 1, 1))
      }
    }
  }
}
