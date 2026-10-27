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

import base.SpecBase
import pages.AddressPage
import play.api.libs.json.Json

class ConfirmedAddressSpec extends SpecBase {
  "Confirmed ALF addresses" - {
    "must preserve long lines, town, postcode, country, organisation, PO box and UPRN through session JSON" in {
      val response = Json.obj(
        "auditRef"     -> "audit-ref",
        "id"           -> "GB123",
        "uprn"         -> 123456789012L,
        "parentUprn"   -> 123456789L,
        "usrn"         -> 12345L,
        "organisation" -> "Organisation",
        "address"      -> Json.obj(
          "lines"        -> Seq("a" * 28, "b" * 35, "c" * 255, "fourth line"),
          "town"         -> ("t" * 40),
          "postcode"     -> "AA1 1AA",
          "country"      -> Json.obj("code" -> "GB", "name" -> "United Kingdom"),
          "organisation" -> "Organisation",
          "poBox"        -> "PO Box 1"
        )
      )
      val address  = response.as[ConfirmedAddress]
      Json.toJson(address) mustBe response
      val answers  = emptyUserAnswers.set(AddressPage, address).success.value
      answers.get(AddressPage).value mustBe address
      address.address.lines.map(_.length) mustBe Seq(28, 35, 255, 11)
    }

    "must accept manually entered international addresses without postcode, OS ID or UPRN" in {
      val response = Json.obj(
        "auditRef" -> "manual-audit",
        "address"  -> Json.obj("lines" -> Seq("Rue Exemple"), "country" -> Json.obj("code" -> "FR", "name" -> "France"))
      )
      val address  = response.as[ConfirmedAddress]
      address.id mustBe None
      address.uprn mustBe None
      address.address.postcode mustBe None
    }

    "must reject malformed ALF responses" in {
      Json.obj("address" -> Json.obj("lines" -> "not a list")).validate[ConfirmedAddress].isError mustBe true
    }
  }
}
