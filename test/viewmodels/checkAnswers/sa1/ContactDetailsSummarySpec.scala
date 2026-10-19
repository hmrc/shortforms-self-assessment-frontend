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

package viewmodels.checkAnswers.sa1

import base.SpecBase
import models.CheckMode
import models.sa1.ContactDetails
import pages.sa1.ContactDetailsPage


class ContactDetailsSummarySpec extends SpecBase {

  private val application = applicationBuilder().build()
  private val msgs = messages(application)

  "ContactDetailsSummary" - {

    "must return no rows when contactDetails answers do not exist" in {
      val result =
        ContactDetailsSummary.rows(userAnswersId, emptyUserAnswers)(msgs)

      result mustBe empty
    }

    "must return two rows when ContactDetails answers exist" in {
      val contactDetails = ContactDetails(
        emailAddress = "name@examplec.com",
        phoneNumber = "+44 1234567890"
      )

      val userAnswers =
        emptyUserAnswers
          .set(ContactDetailsPage, contactDetails)
          .success
          .value

      val result =
        ContactDetailsSummary.rows(userAnswersId, userAnswers)(msgs)

      result must have size 2

      result.head.value.content.toString must include("name@examplec.com")
      result(1).value.content.toString must include("+44 1234567890")

      val expectedUrl =
        controllers.sa1.routes.ContactDetailsController
          .onPageLoad(userAnswersId, CheckMode).url

      result.head.actions.value.items.head.href mustBe expectedUrl
      result(1).actions.value.items.head.href mustBe expectedUrl
    }
  }
}