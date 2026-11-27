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

package pages.cwf1

import base.SpecBase
import models.address.{AddressDetails, ConfirmedAddress}

class IsBusinessAddressSameAsHomeAddressPageSpec extends SpecBase {

  private val businessAddress = ConfirmedAddress(AddressDetails(Seq("1 Test Street")), auditRef = "audit-reference")

  "IsBusinessAddressSameAsHomeAddressPage" - {

    "cleanup" - {

      "must remove BusinessAddressPage when Yes is selected" in {

        val userAnswers = emptyUserAnswers
          .set(BusinessAddressPage, businessAddress)
          .success
          .value

        val updatedUserAnswers = userAnswers
          .set(IsBusinessAddressSameAsHomeAddressPage, true)
          .success
          .value

        updatedUserAnswers.get(BusinessAddressPage) mustBe None
      }

      "must keep BusinessAddressPage when No is selected" in {

        val userAnswers = emptyUserAnswers
          .set(BusinessAddressPage, businessAddress)
          .success
          .value

        val updatedUserAnswers = userAnswers
          .set(IsBusinessAddressSameAsHomeAddressPage, false)
          .success
          .value

        updatedUserAnswers.get(BusinessAddressPage) mustBe Some(businessAddress)
      }
    }
  }
}
