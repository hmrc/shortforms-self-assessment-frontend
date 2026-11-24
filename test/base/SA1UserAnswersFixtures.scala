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

package base

import models.address.{AddressDetails, ConfirmedAddress, Country}
import models.sa1.*
import models.{JourneyType, UserAnswers}
import pages.AddressPage
import pages.sa1.*

import java.time.LocalDate

object SA1UserAnswersFixtures {

  val dob: LocalDate                  = LocalDate.of(1980, 1, 2)
  val moveToAddressOn: LocalDate      = LocalDate.of(2020, 4, 6)
  val companyDirectorSince: LocalDate = LocalDate.of(2015, 4, 6)

  val confirmedAddress: ConfirmedAddress = ConfirmedAddress(
    address = AddressDetails(
      lines = Seq("1 Main Street", "Flat 2"),
      town = Some("London"),
      postcode = Some("SW1A 1AA"),
      country = Some(Country("GB", "United Kingdom"))
    ),
    auditRef = "audit-ref-1"
  )

  val contactDetails: ContactDetails = ContactDetails("jane.doe@example.com", "07123456789")

  def completeAnswers(journeyId: String, userId: String): UserAnswers =
    UserAnswers(journeyId, userId, JourneyType.SA1)
      .set(NamePage, Name("Jane", "Doe"))
      .get
      .set(DateOfBirthPage, dob)
      .get
      .set(AddressPage, confirmedAddress)
      .get
      .set(MoveAddressPage, moveToAddressOn)
      .get
      .set(ContactDetailsPage, contactDetails)
      .get
      .set(DoYouHaveANinoPage, true)
      .get
      .set(EnterNationalInsuranceNumberPage, "AB123456C")
      .get
      .set(AddPreviousLastNamePage, false)
      .get
      .set(WhyTaxReturnPage, Set[WhyTaxReturn](WhyTaxReturn.CompanyDirector))
      .get
      .set(DateUntaxedIncomeDirectorPage, companyDirectorSince)
      .get
}
