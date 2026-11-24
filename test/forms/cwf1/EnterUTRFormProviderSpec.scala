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

class EnterUTRFormProviderSpec extends StringFieldBehaviours {

  val requiredKey  = "cwf1.enterUTR.error.required"
  val lengthKey    = "cwf1.enterUTR.error.length"
  val regexInvalid = "cwf1.enterUTR.error.invalid"
  val exactLength  = 10

  private val form = new EnterUTRFormProvider()()

  ".value" - {

    val fieldName = "value"
    val utr       = "1234567890"

    "bind valid UTR" in {
      val result = form.bind(Map(fieldName -> utr))
      result.get mustBe utr
    }

    "not bind an empty UTR" in {
      val result = form.bind(Map(fieldName -> ""))

      result.errors must contain only FormError(fieldName, requiredKey)
    }

    "not bind a UTR containing invalid characters" in {
      val result = form.bind(Map(fieldName -> "123456789A"))

      result.errors must contain only
        FormError(fieldName, regexInvalid, Seq("^[0-9]+$"))
    }

    "not bind a UTR shorter than the exact length" in {
      val result = form.bind(Map(fieldName -> "123456789"))

      result.errors must contain only FormError(fieldName, lengthKey, Seq(exactLength))
    }

    "not bind a UTR longer than the exact length" in {
      val result = form.bind(Map(fieldName -> "12345678901"))

      result.errors must contain only FormError(fieldName, lengthKey, Seq(exactLength))
    }
  }
}
