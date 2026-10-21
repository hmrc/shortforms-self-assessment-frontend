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

package forms

import forms.behaviours.StringFieldBehaviours
import forms.sa1.ContactDetailsFormProvider
import org.mockito.internal.matchers.Contains
import play.api.data.FormError

class ContactDetailsFormProviderSpec extends StringFieldBehaviours {

  val form = new ContactDetailsFormProvider()()

  ".emailAddress" - {

    val requiredKey = "sa1.contactDetails.error.emailAddress.required"
    val lengthKey   = "sa1.contactDetails.error.emailAddress.length"
    val invalidKey  = "sa1.contactDetails.emailAddress.invalid"
    val maxLength   = 129

    "must bind successfully for a valid email format" in {

      val validEmail = "name@example.com"
      val result     = form.bind(Map("emailAddress" -> validEmail, "phoneNumber" -> "0746689076"))
      result.errors mustBe empty
    }

    "must fail to bind when the domain has no dot extension " in {

      val emailAddress = "name@examplecom"
      val result       = form.bind(Map("emailAddress" -> emailAddress, "phoneNumber" -> "0746689076"))
      result.errors mustBe List(FormError("emailAddress", invalidKey))
    }

    "must fail to bind when the username segment exceeds 64 chars " in {

      val emailAddress = "a" * 65 + "@examplecom"
      val result       = form.bind(Map("emailAddress" -> emailAddress, "phoneNumber" -> "0746689076"))
      result.errors mustBe List(FormError("emailAddress", invalidKey))
    }

    "must fail to bind when the Domain segment exceeds 64 chars " in {

      val emailAddress = "name@" + "n" * 64 + ".com"
      val result       = form.bind(Map("emailAddress" -> emailAddress, "phoneNumber" -> "0746689076"))
      result.errors mustBe List(FormError("emailAddress", invalidKey))
    }

    "must fail to bind when the email id length exceeds 129 chars " in {

      val emailAddress = "a" * 120 + "@" + " e" * 50 + ".com"
      val result       = form.bind(Map("emailAddress" -> emailAddress, "phoneNumber" -> "0746689076"))
      result.errors must contain(FormError("emailAddress", lengthKey, Seq(maxLength)))
    }

    "must fail to bind with error message when emailAddress has empty value" in {

      val emailAddress = ""
      val result       = form.bind(Map("emailAddress" -> emailAddress, "phoneNumber" -> "0746689076"))
      result.errors mustBe List(FormError("emailAddress", requiredKey))
    }
  }

  ".phoneNumber" - {

    val requiredKey = "sa1.contactDetails.error.phoneNumber.required"
    val lengthKey   = "sa1.contactDetails.error.phoneNumber.length"
    val invalidKey  = "sa1.contactDetails.phoneNumber.invalid"
    val maxLength   = 40

    "must bind successfully for a valid phoneNumber format" in {

      val phoneNumber = "0746689076"
      val result      = form.bind(Map("emailAddress" -> "name@example.com", "phoneNumber" -> phoneNumber))
      result.errors mustBe empty
    }

    "must fail to bind when the phoneNumber has invalid data" in {

      val phoneNumber  = "098uydfdfe343"
      val emailAddress = "name@example.com"
      val result       = form.bind(Map("emailAddress" -> emailAddress, "phoneNumber" -> phoneNumber))
      result.errors must contain(FormError("phoneNumber", invalidKey, Seq("^[0-9-()+ ]{0,40}$")))
    }

    "must fail to bind when the phoneNumber length exceeds 40  chars " in {

      val phoneNumber =
        "+ 44 01111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111"
      val result      = form.bind(Map("emailAddress" -> "name@example.com", "phoneNumber" -> phoneNumber))
      result.errors must contain(FormError("phoneNumber", lengthKey, Seq(maxLength)))
    }

    "must fail to bind with error message when phoneNumber has empty value" in {

      val phoneNumber = ""
      val result      = form.bind(Map("emailAddress" -> "name@example.com", "phoneNumber" -> phoneNumber))
      result.errors mustBe List(FormError("phoneNumber", requiredKey))
    }
  }
}
