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

import base.{SA1UserAnswersFixtures, SpecBase}
import models.sa1.*
import models.{JourneyType, UserAnswers}
import pages.sa1.*

import java.time.LocalDate

class SA1SubmissionMapperSpec extends SpecBase {

  private val answers = SA1UserAnswersFixtures.completeAnswers(userAnswersId, testUserId)

  private val expectedSubmission = SA1Submission(
    firstName = "Jane",
    lastName = "Doe",
    previousLastName = None,
    niNumber = Some("AB123456C"),
    niNoReason = None,
    dob = SA1UserAnswersFixtures.dob,
    address = SA1Address(
      line1 = "1 Main Street",
      line2 = "Flat 2",
      town = Some("London"),
      postcode = "SW1A 1AA",
      country = Some("United Kingdom")
    ),
    moveToAddressOn = SA1UserAnswersFixtures.moveToAddressOn,
    phone = "07123456789",
    email = "jane.doe@example.com",
    reasons = SA1Reasons(companyDirectorSince = Some(SA1UserAnswersFixtures.companyDirectorSince)),
    dateLastNameChanged = None
  )

  "SA1SubmissionMapper" - {

    "must map complete answers to the SA1 submission payload" in {
      SA1SubmissionMapper.map(answers) mustBe Right(expectedSubmission)
    }

    "must send the no-NI reason and omit the NI number when the customer has no National Insurance number" in {
      val noNino = answers
        .set(DoYouHaveANinoPage, false)
        .get
        .set(NoNinoReasonPage, "Not yet issued")
        .get

      SA1SubmissionMapper.map(noNino) mustBe Right(
        expectedSubmission.copy(niNumber = None, niNoReason = Some("Not yet issued"))
      )
    }

    "must include the previous last name and change date when the customer has changed their last name" in {
      val withPreviousName = answers
        .set(AddPreviousLastNamePage, true)
        .get
        .set(PreviousLastNamePage, PreviousName("Smith", LocalDate.of(2010, 5, 6)))
        .get

      SA1SubmissionMapper.map(withPreviousName) mustBe Right(
        expectedSubmission.copy(
          previousLastName = Some("Smith"),
          dateLastNameChanged = Some(LocalDate.of(2010, 5, 6))
        )
      )
    }

    "must only send reason dates for reasons the customer selected" in {
      val ukPropertyOnly = answers
        .set(WhyTaxReturnPage, Set[WhyTaxReturn](WhyTaxReturn.UkPropertyIncome))
        .get
        .set(DateLandPropertyPage, LocalDate.of(2018, 1, 1))
        .get

      SA1SubmissionMapper.map(ukPropertyOnly).map(_.reasons) mustBe Right(
        SA1Reasons(dateLandPropertyUk = Some(LocalDate.of(2018, 1, 1)))
      )
    }

    "must not require or send trust settlement income, which the backend does not accept" in {
      val trustOnly = answers
        .set(WhyTaxReturnPage, Set[WhyTaxReturn](WhyTaxReturn.TrustIncome))
        .get

      SA1SubmissionMapper.map(trustOnly) mustBe Right(expectedSubmission.copy(reasons = SA1Reasons()))
    }

    "must include the declaration name as the person completing the form, trimmed" in {
      val named = answers.set(DeclarationPage, "  Jane Doe ").success.value

      SA1SubmissionMapper.map(named).map(_.personCompletingForm) mustBe Right(Some("Jane Doe"))
    }

    "must send no person completing the form when the declaration name is blank" in {
      val blank = answers.set(DeclarationPage, "   ").success.value

      SA1SubmissionMapper.map(blank).map(_.personCompletingForm) mustBe Right(None)
    }

    "must return an error naming the missing page when a required answer is absent" in {
      val empty = UserAnswers(userAnswersId, testUserId, JourneyType.SA1)

      SA1SubmissionMapper.map(empty) mustBe Left("Missing answer: NamePage")
    }

    "must return an error when a selected reason has no date" in {
      val foreignIncomeWithoutDate = answers
        .set(WhyTaxReturnPage, Set[WhyTaxReturn](WhyTaxReturn.ForeignIncome))
        .get

      SA1SubmissionMapper.map(foreignIncomeWithoutDate) mustBe Left(
        "Missing answer: TaxableForeignIncomeDatePage"
      )
    }

    "must return an error when the address is missing" in {
      val withoutAddress = UserAnswers(userAnswersId, testUserId, JourneyType.SA1)
        .set(NamePage, Name("Jane", "Doe"))
        .get
        .set(DateOfBirthPage, SA1UserAnswersFixtures.dob)
        .get

      SA1SubmissionMapper.map(withoutAddress) mustBe Left("Missing answer: AddressPage")
    }
  }
}
