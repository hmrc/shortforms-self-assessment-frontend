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

import play.api.libs.json.*

import java.time.LocalDate

final case class SA1Address(
  line1: String,
  line2: String,
  town: Option[String],
  postcode: String,
  country: Option[String]
)

object SA1Address {
  implicit val format: OFormat[SA1Address] = Json.format
}

final case class SA1Reasons(
  companyDirectorSince: Option[LocalDate] = None,
  dateLandPropertyUk: Option[LocalDate] = None,
  taxableForeignIncomeDate: Option[LocalDate] = None,
  dateAnnualIncome: Option[LocalDate] = None,
  untaxedIncomeNotInPayeStartDate: Option[LocalDate] = None,
  highIncomeChildBenefitDate: Option[LocalDate] = None,
  dateCapitalGainsTax: Option[LocalDate] = None,
  otherReason: Option[String] = None,
  otherReasonAppliesSince: Option[LocalDate] = None
)

object SA1Reasons {
  implicit val format: OFormat[SA1Reasons] = Json.format
}

final case class SA1Submission(
  firstName: String,
  lastName: String,
  previousLastName: Option[String],
  niNumber: Option[String],
  niNoReason: Option[String],
  dob: LocalDate,
  address: SA1Address,
  moveToAddressOn: LocalDate,
  phone: String,
  email: String,
  reasons: SA1Reasons,
  dateLastNameChanged: Option[LocalDate],
  personCompletingForm: Option[String] = None
)

object SA1Submission {
  implicit val format: OFormat[SA1Submission] = Json.format
}
