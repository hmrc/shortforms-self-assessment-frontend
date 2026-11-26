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

package forms.cwf1

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class ContactDetailsFormProviderSpec extends StringFieldBehaviours {

  val form = new ContactDetailsFormProvider()()

  ".emailAddress" - {

    val fieldName   = "emailAddress"
    val requiredKey = "cwf1.contactDetails.error.emailAddress.required"
    val invalidKey  = "cwf1.contactDetails.error.emailAddress.invalid"
    val lengthKey   = "cwf1.contactDetails.error.emailAddress.length"
    val maxLength   = 129

    "must bind successfully for a valid email format" in {
      val validEmail = "name@example.com"

      val result = form.bind(Map(fieldName -> validEmail, "phoneNumber" -> "0746689076"))
      result.errors mustBe empty
    }

    "must fail to bind when emailAddress is invalid" in {
      val invalidEmailAddress = "not-an-email"

      val result = form.bind(Map(fieldName -> invalidEmailAddress, "phoneNumber" -> "0746689076"))
      result.errors mustBe List(
        FormError(fieldName, invalidKey)
      )
    }

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }

  ".phoneNumber" - {

    val fieldName   = "phoneNumber"
    val requiredKey = "cwf1.contactDetails.error.phoneNumber.required"
    val invalidKey  = "cwf1.contactDetails.error.phoneNumber.invalid"
    val lengthKey   = "cwf1.contactDetails.error.phoneNumber.length"
    val maxLength   = 40

    "must bind successfully for a valid phoneNumber format" in {
      val phoneNumber = "0746689076"

      val result = form.bind(Map("emailAddress" -> "name@example.com", fieldName -> phoneNumber))
      result.errors mustBe empty
    }

    "must fail to bind when the phoneNumber has invalid data" in {
      val phoneNumber  = "098uydfdfe343"
      val emailAddress = "name@example.com"

      val result = form.bind(Map("emailAddress" -> emailAddress, fieldName -> phoneNumber))
      result.errors must contain(FormError(fieldName, invalidKey, Seq("^[0-9-()+ ]{0,40}$")))
    }

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
