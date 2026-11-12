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

import base.SpecBase
import models.UserAnswers
import models.address.{AddressDetails, ConfirmedAddress, Country}
import models.sa1.*
import pages.AddressPage
import pages.sa1.*

import java.time.LocalDate

class CheckYourAnswersValidatorSpec extends SpecBase {

  "CheckYourAnswersValidator" - {
    ".areRequiredDataPresent" - {
      "must return true when all required answers are present" in new Setup {
        checkYourAnswersValidator.areRequiredDataPresent(completeUserAnswers) mustBe true
      }

      "must return false when the name is missing" in new Setup {
        val userAnswers = completeUserAnswers.remove(NamePage).success.value
        checkYourAnswersValidator.areRequiredDataPresent(userAnswers) mustBe false
      }

      "must return false when the previous last name question has not been answered" in new Setup {
        val userAnswers = completeUserAnswers.remove(AddPreviousLastNamePage).success.value
        checkYourAnswersValidator.areRequiredDataPresent(userAnswers) mustBe false
      }

      "must return true when the user does not have a previous last name" in new Setup {
        val userAnswers = completeUserAnswers
          .set(AddPreviousLastNamePage, false)
          .success
          .value
          .remove(PreviousLastNamePage)
          .success
          .value
        checkYourAnswersValidator.areRequiredDataPresent(userAnswers) mustBe true
      }

      "must return true when the user has a previous last name and has provided it" in new Setup {
        val userAnswers = completeUserAnswers
          .set(AddPreviousLastNamePage, true)
          .success
          .value
          .set(PreviousLastNamePage, PreviousName("Previous Name", LocalDate.of(2000, 10, 26)))
          .success
          .value
        checkYourAnswersValidator.areRequiredDataPresent(userAnswers) mustBe true
      }

      "must return false when the user has a previous last name but has not provided it" in new Setup {
        val userAnswers = completeUserAnswers
          .set(AddPreviousLastNamePage, true)
          .success
          .value
          .remove(PreviousLastNamePage)
          .success
          .value
        checkYourAnswersValidator.areRequiredDataPresent(userAnswers) mustBe false
      }

      "must return false when the date of birth is missing" in new Setup {
        val userAnswers = completeUserAnswers
          .remove(DateOfBirthPage)
          .success
          .value
        checkYourAnswersValidator.areRequiredDataPresent(userAnswers) mustBe false
      }

      "must return false when the contact details are missing" in new Setup {
        val userAnswers = completeUserAnswers
          .remove(ContactDetailsPage)
          .success
          .value
        checkYourAnswersValidator.areRequiredDataPresent(userAnswers) mustBe false
      }

      "must return false when the address is missing" in new Setup {
        val userAnswers = completeUserAnswers
          .remove(AddressPage)
          .success
          .value
        checkYourAnswersValidator.areRequiredDataPresent(userAnswers) mustBe false
      }

      "must return false when the move address question has not been answered" in new Setup {
        val userAnswers = completeUserAnswers
          .remove(MoveAddressPage)
          .success
          .value
        checkYourAnswersValidator.areRequiredDataPresent(userAnswers) mustBe false
      }

      "must return false when the NINO question has not been answered" in new Setup {
        val userAnswers = completeUserAnswers
          .remove(DoYouHaveANinoPage)
          .success
          .value
        checkYourAnswersValidator.areRequiredDataPresent(userAnswers) mustBe false
      }

      "must return true when the user has a NINO and has provided it" in new Setup {
        val userAnswers = completeUserAnswers
          .set(DoYouHaveANinoPage, true)
          .success
          .value
          .set(EnterNationalInsuranceNumberPage, "AB123456A")
          .success
          .value
        checkYourAnswersValidator.areRequiredDataPresent(userAnswers) mustBe true
      }

      "must return false when the user has a NINO but has not provided it" in new Setup {
        val userAnswers = completeUserAnswers
          .set(DoYouHaveANinoPage, true)
          .success
          .value
          .remove(EnterNationalInsuranceNumberPage)
          .success
          .value
        checkYourAnswersValidator.areRequiredDataPresent(userAnswers) mustBe false
      }

      "must return true when the user does not have a NINO and has provided a reason" in new Setup {
        val userAnswers = completeUserAnswers
          .set(DoYouHaveANinoPage, false)
          .success
          .value
          .set(NoNinoReasonPage, "Reason")
          .success
          .value
        checkYourAnswersValidator.areRequiredDataPresent(userAnswers) mustBe true
      }

      "must return false when the user does not have a NINO but has not provided a reason" in new Setup {
        val userAnswers = completeUserAnswers
          .set(DoYouHaveANinoPage, false)
          .success
          .value
          .remove(NoNinoReasonPage)
          .success
          .value
        checkYourAnswersValidator.areRequiredDataPresent(userAnswers) mustBe false
      }

      "must return false when the tax return reasons have not been provided" in new Setup {
        val userAnswers = completeUserAnswers
          .remove(WhyTaxReturnPage)
          .success
          .value
        checkYourAnswersValidator.areRequiredDataPresent(userAnswers) mustBe false
      }

      "must return true when Company Director is selected and the date has been provided" in new Setup {
        val userAnswers = completeUserAnswers
          .set(WhyTaxReturnPage, Set(WhyTaxReturn.CompanyDirector))
          .success
          .value
          .set(DateUntaxedIncomeDirectorPage, LocalDate.of(2020, 1, 1))
          .success
          .value
        checkYourAnswersValidator.areRequiredDataPresent(userAnswers) mustBe true
      }

      "must return false when Company Director is selected and the date is missing" in new Setup {
        val userAnswers = completeUserAnswers
          .set(WhyTaxReturnPage, Set(WhyTaxReturn.CompanyDirector))
          .success
          .value
          .remove(DateUntaxedIncomeDirectorPage)
          .success
          .value
        checkYourAnswersValidator.areRequiredDataPresent(userAnswers) mustBe false
      }

      "must return true when UK Property Income is selected and the date has been provided" in new Setup {
        val userAnswers = completeUserAnswers
          .set(WhyTaxReturnPage, Set(WhyTaxReturn.UkPropertyIncome))
          .success
          .value
          .set(DateLandPropertyPage, LocalDate.of(2020, 1, 1))
          .success
          .value
        checkYourAnswersValidator.areRequiredDataPresent(userAnswers) mustBe true
      }

      "must return false when UK Property Income is selected and the date is missing" in new Setup {
        val userAnswers = completeUserAnswers
          .set(WhyTaxReturnPage, Set(WhyTaxReturn.UkPropertyIncome))
          .success
          .value
          .remove(DateLandPropertyPage)
          .success
          .value
        checkYourAnswersValidator.areRequiredDataPresent(userAnswers) mustBe false
      }

      "must return true when Foreign Income is selected and the date has been provided" in new Setup {
        val userAnswers = completeUserAnswers
          .set(WhyTaxReturnPage, Set(WhyTaxReturn.ForeignIncome))
          .success
          .value
          .set(TaxableForeignIncomeDatePage, LocalDate.of(2020, 1, 1))
          .success
          .value
        checkYourAnswersValidator.areRequiredDataPresent(userAnswers) mustBe true
      }

      "must return false when Foreign Income is selected and the date is missing" in new Setup {
        val userAnswers = completeUserAnswers
          .set(WhyTaxReturnPage, Set(WhyTaxReturn.ForeignIncome))
          .success
          .value
          .remove(TaxableForeignIncomeDatePage)
          .success
          .value
        checkYourAnswersValidator.areRequiredDataPresent(userAnswers) mustBe false
      }

      "must return true when Trust Income is selected and the date has been provided" in new Setup {
        val userAnswers = completeUserAnswers
          .set(WhyTaxReturnPage, Set(WhyTaxReturn.TrustIncome))
          .success
          .value
          .set(TrustSettlementDatePage, LocalDate.of(2020, 1, 1))
          .success
          .value
        checkYourAnswersValidator.areRequiredDataPresent(userAnswers) mustBe true
      }

      "must return false when Trust Income is selected and the date is missing" in new Setup {
        val userAnswers = completeUserAnswers
          .set(WhyTaxReturnPage, Set(WhyTaxReturn.TrustIncome))
          .success
          .value
          .remove(TrustSettlementDatePage)
          .success
          .value
        checkYourAnswersValidator.areRequiredDataPresent(userAnswers) mustBe false
      }

      "must return true when Income Thresholds is selected and the date has been provided" in new Setup {
        val userAnswers = completeUserAnswers
          .set(WhyTaxReturnPage, Set(WhyTaxReturn.IncomeThresholds))
          .success
          .value
          .set(DateAnnualIncomePage, LocalDate.of(2020, 1, 1))
          .success
          .value
        checkYourAnswersValidator.areRequiredDataPresent(userAnswers) mustBe true
      }

      "must return false when Income Thresholds is selected and the date is missing" in new Setup {
        val userAnswers = completeUserAnswers
          .set(WhyTaxReturnPage, Set(WhyTaxReturn.IncomeThresholds))
          .success
          .value
          .remove(DateAnnualIncomePage)
          .success
          .value
        checkYourAnswersValidator.areRequiredDataPresent(userAnswers) mustBe false
      }

      "must return true when Untaxed Income is selected and the date has been provided" in new Setup {
        val userAnswers = completeUserAnswers
          .set(WhyTaxReturnPage, Set(WhyTaxReturn.UntaxedIncome))
          .success
          .value
          .set(UntaxedIncomeNotInPayeStartDatePage, LocalDate.of(2020, 1, 1))
          .success
          .value
        checkYourAnswersValidator.areRequiredDataPresent(userAnswers) mustBe true
      }

      "must return false when Untaxed Income is selected and the date is missing" in new Setup {
        val userAnswers = completeUserAnswers
          .set(WhyTaxReturnPage, Set(WhyTaxReturn.UntaxedIncome))
          .success
          .value
          .remove(UntaxedIncomeNotInPayeStartDatePage)
          .success
          .value
        checkYourAnswersValidator.areRequiredDataPresent(userAnswers) mustBe false
      }

      "must return true when Child Benefit Charge is selected and the date has been provided" in new Setup {
        val userAnswers = completeUserAnswers
          .set(WhyTaxReturnPage, Set(WhyTaxReturn.ChildBenefitCharge))
          .success
          .value
          .set(HighIncomeChildBenefitDatePage, LocalDate.of(2020, 1, 1))
          .success
          .value
        checkYourAnswersValidator.areRequiredDataPresent(userAnswers) mustBe true
      }

      "must return false when Child Benefit Charge is selected and the date is missing" in new Setup {
        val userAnswers = completeUserAnswers
          .set(WhyTaxReturnPage, Set(WhyTaxReturn.ChildBenefitCharge))
          .success
          .value
          .remove(HighIncomeChildBenefitDatePage)
          .success
          .value
        checkYourAnswersValidator.areRequiredDataPresent(userAnswers) mustBe false
      }

      "must return true when Capital Gains Tax is selected and the date has been provided" in new Setup {
        val userAnswers = completeUserAnswers
          .set(WhyTaxReturnPage, Set(WhyTaxReturn.CapitalGainsTax))
          .success
          .value
          .set(DateCapitalGainsTaxPage, LocalDate.of(2020, 1, 1))
          .success
          .value
        checkYourAnswersValidator.areRequiredDataPresent(userAnswers) mustBe true
      }

      "must return false when Capital Gains Tax is selected and the date is missing" in new Setup {
        val userAnswers = completeUserAnswers
          .set(WhyTaxReturnPage, Set(WhyTaxReturn.CapitalGainsTax))
          .success
          .value
          .remove(DateCapitalGainsTaxPage)
          .success
          .value
        checkYourAnswersValidator.areRequiredDataPresent(userAnswers) mustBe false
      }

      "must return true when Other is selected and a reason date has been provided" in new Setup {
        val userAnswers = completeUserAnswers
          .set(WhyTaxReturnPage, Set(WhyTaxReturn.Other))
          .success
          .value
          .set(OtherReasonPage, OtherReason("Reason", LocalDate.of(2020, 1, 1)))
          .success
          .value
        checkYourAnswersValidator.areRequiredDataPresent(userAnswers) mustBe true
      }

      "must return false when Other is selected and the reason and date are missing" in new Setup {
        val userAnswers = completeUserAnswers
          .set(WhyTaxReturnPage, Set(WhyTaxReturn.Other))
          .success
          .value
          .remove(OtherReasonPage)
          .success
          .value
        checkYourAnswersValidator.areRequiredDataPresent(userAnswers) mustBe false
      }

      "must return true when multiple tax return reasons are selected and all required dates are present" in new Setup {
        val userAnswers = completeUserAnswers
          .set(WhyTaxReturnPage, Set(WhyTaxReturn.IncomeThresholds, WhyTaxReturn.UntaxedIncome))
          .success
          .value
          .set(DateAnnualIncomePage, LocalDate.of(2020, 1, 1))
          .success
          .value
          .set(UntaxedIncomeNotInPayeStartDatePage, LocalDate.of(2020, 1, 1))
          .success
          .value
        checkYourAnswersValidator.areRequiredDataPresent(userAnswers) mustBe true
      }

      "must return false when multiple tax return reasons are selected and at least one required date is missing" in new Setup {
        val userAnswers = completeUserAnswers
          .set(WhyTaxReturnPage, Set(WhyTaxReturn.IncomeThresholds, WhyTaxReturn.UntaxedIncome))
          .success
          .value
          .set(DateAnnualIncomePage, LocalDate.of(2020, 1, 1))
          .success
          .value
          .remove(UntaxedIncomeNotInPayeStartDatePage)
          .success
          .value
        checkYourAnswersValidator.areRequiredDataPresent(userAnswers) mustBe false
      }
    }
  }

  trait Setup {
    lazy val completeUserAnswers: UserAnswers = emptyUserAnswers
      .set(NamePage, Name("Jane", "Doe"))
      .success
      .value
      .set(AddPreviousLastNamePage, true)
      .success
      .value
      .set(PreviousLastNamePage, PreviousName("Jones", LocalDate.of(2000, 10, 26)))
      .success
      .value
      .set(DateOfBirthPage, LocalDate.of(2000, 10, 26))
      .success
      .value
      .set(ContactDetailsPage, ContactDetails("email", "+123456789"))
      .success
      .value
      .set(
        AddressPage,
        ConfirmedAddress(
          AddressDetails(
            Seq("<script>alert('address')</script>", "A very long address line that is not truncated"),
            town = Some("Town"),
            postcode = Some("AA1 1AA"),
            country = Some(Country("GB", "United Kingdom"))
          ),
          auditRef = "audit-ref",
          uprn = Some(998877665544L)
        )
      )
      .success
      .value
      .set(MoveAddressPage, LocalDate.of(2000, 10, 26))
      .success
      .value
      .set(DoYouHaveANinoPage, true)
      .success
      .value
      .set(EnterNationalInsuranceNumberPage, "A123456788")
      .success
      .value
      .set(WhyTaxReturnPage, Set(WhyTaxReturn.CompanyDirector))
      .success
      .value
      .set(DateUntaxedIncomeDirectorPage, LocalDate.of(2020, 1, 1))
      .success
      .value

    val checkYourAnswersValidator = new CheckYourAnswersValidator()
  }
}
