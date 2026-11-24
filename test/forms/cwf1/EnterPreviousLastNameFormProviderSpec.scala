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

class EnterPreviousLastNameFormProviderSpec extends StringFieldBehaviours {

  private val form = new EnterPreviousLastNameFormProvider()()

  private val maxLength = 100

  private val requiredError    = "cwf1.enterPreviousLastName.error.required"
  private val lengthError      = "cwf1.enterPreviousLastName.error.length"
  private val invalidNameError = "cwf1.enterPreviousLastName.error.invalid"

  private val validData: Map[String, String] = Map(
    "value" -> "John Doe"
  )

  "PreviousNameFormProvider" - {

    "must bind valid data" in {
      val result = form.bind(validData)

      result.hasErrors mustBe false
      result.value.value mustBe "John Doe"
    }

    "must trim whitespace from the previous last name" in {
      val result = form.bind(validData.updated("value", "  John Doe  "))

      result.value.value mustBe "John Doe"
    }

    "must reject a previous last name containing only whitespace" in {
      val result = form.bind(validData.updated("value", "   "))

      result.errors must contain(FormError("value", requiredError))
    }

    "must reject a previous last name containing numbers" in {
      val result = form.bind(validData.updated("value", "Smith123"))

      result.errors must contain(
        FormError("value", invalidNameError, Seq("^[A-Za-z-' ]{1,100}$"))
      )
    }

    "must reject unsupported characters in the previous last name" in {
      val result = form.bind(validData.updated("value", "Smith@Jones"))

      result.errors must contain(
        FormError("value", invalidNameError, Seq("^[A-Za-z-' ]{1,100}$"))
      )
    }

    "must accept apostrophes, hyphens and spaces in a previous last name" in {
      val result = form.bind(validData.updated("value", "O'Brien-Smith Jones"))

      result.hasErrors mustBe false
    }

    "must reject a previous last name longer than 100 characters" in {
      val result = form.bind(validData.updated("value", "Aa" * maxLength))

      result.errors must contain(
        FormError("value", lengthError, Seq(maxLength))
      )
    }

    "must accept a previous last name of exactly 100 characters" in {
      val result = form.bind(validData.updated("value", "A" * maxLength))

      result.errors.filter(_.key == "value") mustBe empty
    }

  }
}
