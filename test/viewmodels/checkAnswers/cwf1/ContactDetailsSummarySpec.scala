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

package viewmodels.checkAnswers.cwf1

import base.SpecBase
import models.CheckMode
import models.cwf1.ContactDetails
import pages.cwf1.ContactDetailsPage
import play.api.i18n.Messages

class ContactDetailsSummarySpec extends SpecBase {
  private given Messages = play.api.test.Helpers.stubMessages()

  "ContactDetailsSummary" - {
    "must return no rows when answers do not exist" in {
      val rows =
        ContactDetailsSummary.rows(emptyUserAnswers)

      rows mustBe empty
    }

    "must return two rows when answers exist" in {
      val contactDetails = ContactDetails(
        emailAddress = "name@examplec.com",
        phoneNumber = "+44 1234567890"
      )

      val userAnswers =
        emptyUserAnswers
          .set(ContactDetailsPage, contactDetails)
          .success
          .value

      val rows =
        ContactDetailsSummary.rows(userAnswers)

      rows must have size 2

      rows.head.value.content.toString must include("name@examplec.com")
      rows(1).value.content.toString   must include("+44 1234567890")

      val expectedUrl =
        controllers.cwf1.routes.ContactDetailsController
          .onPageLoad(userAnswersId, CheckMode)
          .url

      rows.head.actions.value.items.head.href mustBe expectedUrl
      rows(1).actions.value.items.head.href mustBe expectedUrl

      val actionItem1 = rows.head.actions.value.items

      actionItem1.head.content.toString must include("site.change")
      actionItem1.head.visuallyHiddenText.value mustBe "cwf1.contactDetails.emailAddress.change.hidden"
      actionItem1.head.attributes("id") mustBe "contact-details-email-address"

      val actionItem2 = rows(1).actions.value.items

      actionItem2.head.content.toString must include("site.change")
      actionItem2.head.visuallyHiddenText.value mustBe "cwf1.contactDetails.phoneNumber.change.hidden"
      actionItem2.head.attributes("id") mustBe "contact-details-phone-number"
    }
  }
}
