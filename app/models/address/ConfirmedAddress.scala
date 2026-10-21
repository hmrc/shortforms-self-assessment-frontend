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

package models.address

import play.api.libs.json.*

case class Country(code: String, name: String)

object Country {
  implicit val format: OFormat[Country] = Json.format[Country]
}

case class AddressDetails(
  lines: Seq[String],
  town: Option[String] = None,
  postcode: Option[String] = None,
  country: Option[Country] = None,
  organisation: Option[String] = None,
  poBox: Option[String] = None
) {
  def displayLines: Seq[String] =
    organisation.toSeq ++ lines ++ town.toSeq ++ postcode.toSeq ++ country.map(_.name).toSeq
}

object AddressDetails {
  implicit val format: OFormat[AddressDetails] = Json.format[AddressDetails]
}

case class ConfirmedAddress(
  address: AddressDetails,
  auditRef: String,
  id: Option[String] = None,
  uprn: Option[Long] = None,
  parentUprn: Option[Long] = None,
  usrn: Option[Long] = None,
  organisation: Option[String] = None
)

object ConfirmedAddress {
  implicit val format: OFormat[ConfirmedAddress] = Json.format[ConfirmedAddress]
}
