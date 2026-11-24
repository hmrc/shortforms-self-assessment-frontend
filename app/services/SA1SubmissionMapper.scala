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

package services

import models.UserAnswers
import models.address.ConfirmedAddress
import models.sa1.*
import pages.AddressPage
import pages.sa1.*

object SA1SubmissionMapper {

  def map(answers: UserAnswers): Either[String, SA1Submission] =
    for {
      name            <- required(answers.get(NamePage), "NamePage")
      dob             <- required(answers.get(DateOfBirthPage), "DateOfBirthPage")
      address         <- required(answers.get(AddressPage), "AddressPage")
      moveToAddressOn <- required(answers.get(MoveAddressPage), "MoveAddressPage")
      contact         <- required(answers.get(ContactDetailsPage), "ContactDetailsPage")
      nino            <- ninoFields(answers)
      previous        <- previousName(answers)
      sa1Reasons      <- reasons(answers)
    } yield SA1Submission(
      firstName = name.firstName,
      lastName = name.lastName,
      previousLastName = previous.map(_.previousLastName),
      niNumber = nino._1,
      niNoReason = nino._2,
      dob = dob,
      address = toAddress(address),
      moveToAddressOn = moveToAddressOn,
      phone = contact.phoneNumber,
      email = contact.emailAddress,
      reasons = sa1Reasons,
      dateLastNameChanged = previous.map(_.lastNameChangeDate),
      personCompletingForm = answers.get(DeclarationPage).map(_.trim).filter(_.nonEmpty)
    )

  private def required[A](value: Option[A], pageName: String): Either[String, A] =
    value.toRight(s"Missing answer: $pageName")

  private def ninoFields(answers: UserAnswers): Either[String, (Option[String], Option[String])] =
    required(answers.get(DoYouHaveANinoPage), "DoYouHaveANinoPage").flatMap {
      case true  =>
        required(answers.get(EnterNationalInsuranceNumberPage), "EnterNationalInsuranceNumberPage")
          .map(nino => (Some(nino), None))
      case false =>
        required(answers.get(NoNinoReasonPage), "NoNinoReasonPage")
          .map(reason => (None, Some(reason)))
    }

  private def previousName(answers: UserAnswers): Either[String, Option[PreviousName]] =
    required(answers.get(AddPreviousLastNamePage), "AddPreviousLastNamePage").flatMap {
      case true  => required(answers.get(PreviousLastNamePage), "PreviousLastNamePage").map(Some(_))
      case false => Right(None)
    }

  private def toAddress(confirmed: ConfirmedAddress): SA1Address = {
    val lines = confirmed.address.lines
    SA1Address(
      line1 = lines.headOption.getOrElse(""),
      line2 = lines.lift(1).getOrElse(""),
      town = confirmed.address.town,
      postcode = confirmed.address.postcode.getOrElse(""),
      country = confirmed.address.country.map(_.name)
    )
  }

  private def reasons(answers: UserAnswers): Either[String, SA1Reasons] =
    required(answers.get(WhyTaxReturnPage), "WhyTaxReturnPage").flatMap { selected =>
      for {
        companyDirectorSince            <- ifSelected(selected, WhyTaxReturn.CompanyDirector, "DateUntaxedIncomeDirectorPage")(
                                             answers.get(DateUntaxedIncomeDirectorPage)
                                           )
        dateLandPropertyUk              <- ifSelected(selected, WhyTaxReturn.UkPropertyIncome, "DateLandPropertyPage")(
                                             answers.get(DateLandPropertyPage)
                                           )
        taxableForeignIncomeDate        <- ifSelected(selected, WhyTaxReturn.ForeignIncome, "TaxableForeignIncomeDatePage")(
                                             answers.get(TaxableForeignIncomeDatePage)
                                           )
        dateAnnualIncome                <- ifSelected(selected, WhyTaxReturn.IncomeThresholds, "DateAnnualIncomePage")(
                                             answers.get(DateAnnualIncomePage)
                                           )
        untaxedIncomeNotInPayeStartDate <-
          ifSelected(selected, WhyTaxReturn.UntaxedIncome, "UntaxedIncomeNotInPayeStartDatePage")(
            answers.get(UntaxedIncomeNotInPayeStartDatePage)
          )
        highIncomeChildBenefitDate      <-
          ifSelected(selected, WhyTaxReturn.ChildBenefitCharge, "HighIncomeChildBenefitDatePage")(
            answers.get(HighIncomeChildBenefitDatePage)
          )
        dateCapitalGainsTax             <- ifSelected(selected, WhyTaxReturn.CapitalGainsTax, "DateCapitalGainsTaxPage")(
                                             answers.get(DateCapitalGainsTaxPage)
                                           )
        other                           <- ifSelected(selected, WhyTaxReturn.Other, "OtherReasonPage")(
                                             answers.get(OtherReasonPage)
                                           )
      } yield SA1Reasons(
        companyDirectorSince = companyDirectorSince,
        dateLandPropertyUk = dateLandPropertyUk,
        taxableForeignIncomeDate = taxableForeignIncomeDate,
        dateAnnualIncome = dateAnnualIncome,
        untaxedIncomeNotInPayeStartDate = untaxedIncomeNotInPayeStartDate,
        highIncomeChildBenefitDate = highIncomeChildBenefitDate,
        dateCapitalGainsTax = dateCapitalGainsTax,
        otherReason = other.map(_.reason),
        otherReasonAppliesSince = other.map(_.reasonDate)
      )
    }

  private def ifSelected[A](
    selected: Set[WhyTaxReturn],
    reason: WhyTaxReturn,
    pageName: String
  )(value: => Option[A]): Either[String, Option[A]] =
    if (selected.contains(reason)) required(value, pageName).map(Some(_))
    else Right(None)
}
