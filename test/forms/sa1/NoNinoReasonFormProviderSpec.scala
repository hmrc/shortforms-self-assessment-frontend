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

package forms.sa1

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class NoNinoReasonFormProviderSpec extends StringFieldBehaviours {

  private val requiredKey = "sa1.noNinoReason.error.required"
  private val lengthKey   = "sa1.noNinoReason.error.length"
  private val fieldName   = "value"
  private val maxLength   = 2000

  private val form = new NoNinoReasonFormProvider()()

  ".value" - {

    val validReason = "No Nino"

    "bind valid reasons" in {
      val result = form.bind(Map(fieldName -> validReason))

      result.get mustBe validReason
    }

    "not bind an empty reason" in {
      val result = form.bind(Map(fieldName -> ""))

      result.errors must contain only FormError(fieldName, requiredKey)
    }

    "not bind a missing reason" in {
      val result = form.bind(Map.empty[String, String])

      result.errors must contain only FormError(fieldName, requiredKey)
    }

    "not bind a reason containing only whitespace" in {
      val result = form.bind(Map(fieldName -> "   "))

      result.errors must contain only FormError(fieldName, requiredKey)
    }

    "bind a reason at the maximum length" in {
      val reason = "a" * maxLength
      val result = form.bind(Map(fieldName -> reason))

      result.get mustBe reason
    }

    "not bind a reason longer than the maximum length" in {
      val reason = "a" * (maxLength + 1)
      val result = form.bind(Map(fieldName -> reason))

      result.errors must contain only FormError(fieldName, lengthKey, Seq(maxLength))
    }

    fieldWithMaxLength(
      form,
      fieldName,
      maxLength,
      FormError(fieldName, lengthKey, Seq(maxLength))
    )
  }
}
