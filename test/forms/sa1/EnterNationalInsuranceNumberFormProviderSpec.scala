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

class EnterNationalInsuranceNumberFormProviderSpec extends StringFieldBehaviours {

  val requiredKey  = "sa1.enterNationalInsuranceNumber.error.required"
  val lengthKey    = "sa1.enterNationalInsuranceNumber.error.length"
  val regexInvalid = "sa1.enterNationalInsuranceNumber.error.invalid"
  val maxLength    = 9

  private val form = new EnterNationalInsuranceNumberFormProvider()()

  ".value" - {

    val fieldName = "value"
    val nino      = "NM772208B"

    "bind valid nino" in {

      val result = form.bind(Map(fieldName -> nino))
      result.get mustBe nino
    }

    "not bind an empty nino" in {
      val result = form.bind(Map(fieldName -> ""))

      result.errors must contain only FormError(fieldName, requiredKey)
    }

    "not bind a nino containing invalid format" in {
      val result = form.bind(Map(fieldName -> "AB%$$$$$$"))

      result.errors.contains(regexInvalid)
    }

    "not bind a nino longer than the maximum length" in {
      val nino   = "NM772208BB"
      val result = form.bind(Map(fieldName -> nino))

      result.errors must contain only (FormError(fieldName, lengthKey, Seq(maxLength)),
      FormError(fieldName, regexInvalid, Seq("^[A-Za-z]{2}[0-9]{6}[A-Za-z ]?$")))
    }
  }
}
