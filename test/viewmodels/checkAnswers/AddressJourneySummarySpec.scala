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

package viewmodels.checkAnswers

import base.SpecBase
import models.address.{AddressDetails, ConfirmedAddress}
import pages.AddressPage
import play.api.test.Helpers.stubMessages

class AddressJourneySummarySpec extends SpecBase {

  private val msgs = stubMessages()

  "AddressJourneySummary" - {

    "must return no rows when address answer does not exist" in {
      val result = AddressJourneySummary.rows(emptyUserAnswers)(msgs)

      result mustBe empty
    }

    "must return one row when address answer exists" in {
      val confirmedAddress = ConfirmedAddress(
        address = AddressDetails(
          lines = Seq("10 Downing Street"),
          town = Some("London"),
          postcode = Some("SW1A 2AA")
        ),
        auditRef = "audit-reference"
      )

      val userAnswers =
        emptyUserAnswers
          .set(AddressPage, confirmedAddress)
          .success
          .value

      val result = AddressJourneySummary.rows(userAnswers)(msgs)

      result must have size 1

      val addressContent = result.head.value.content.asHtml.toString

      addressContent must include("10 Downing Street")
      addressContent must include("London")
      addressContent must include("SW1A 2AA")

      result.head.key.content.asHtml.toString must include(
        msgs("sa1.address.checkYourAnswersLabel")
      )

      result.head.actions.value.items.head.href mustBe
        controllers.sa1.routes.AddressLookupController.change(userAnswersId).url

      result.head.actions.value.items.head.content.asHtml.toString must include(
        msgs("site.change")
      )

      result.head.actions.value.items.head.visuallyHiddenText.value mustBe
        msgs("sa1.address.change.hidden")
    }

    "must HTML-escape address display lines" in {
      val confirmedAddress = ConfirmedAddress(
        address = AddressDetails(
          lines = Seq("<b>10 Downing Street</b>", "London & Westminster")
        ),
        auditRef = "audit-reference"
      )

      val userAnswers =
        emptyUserAnswers
          .set(AddressPage, confirmedAddress)
          .success
          .value

      val result = AddressJourneySummary.rows(userAnswers)(msgs)

      val addressContent = result.head.value.content.asHtml.toString

      addressContent must include("&lt;b&gt;10 Downing Street&lt;/b&gt;")
      addressContent must include("London &amp; Westminster")
    }
  }
}
