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

import forms.FormSpec
import models.sa1.Name
import play.api.data.FormError

class NameFormProviderSpec extends FormSpec {

  private val form = new NameFormProvider()()

  private val maxLength = 100

  "NameFormProvider" - {

    "must bind valid first and last names" in {
      val result = form.bind(Map("firstName" -> "Jane", "lastName" -> "Doe"))

      result.value.value mustEqual Name("Jane", "Doe")
      result.errors mustBe empty
    }

    "must trim surrounding whitespace from both names" in {
      val result = form.bind(Map("firstName" -> "  Jane ", "lastName" -> " Doe  "))

      result.value.value mustEqual Name("Jane", "Doe")
    }

    "must accept letters, apostrophes, hyphens and spaces" in {
      val result = form.bind(Map("firstName" -> "Mary-Jane", "lastName" -> "O'Neil Smith"))

      result.value.value mustEqual Name("Mary-Jane", "O'Neil Smith")
    }

    "must bind a name of exactly 100 characters" in {
      val name   = "a" * maxLength
      val result = form.bind(Map("firstName" -> name, "lastName" -> name))

      result.value.value mustEqual Name(name, name)
    }

    "must not bind when the first name is missing" in {
      val result = form.bind(Map("lastName" -> "Doe")).apply("firstName")

      result.errors mustEqual Seq(FormError("firstName", "sa1.name.firstName.error.required"))
    }

    "must not bind when the first name is blank" in {
      val result = form.bind(Map("firstName" -> "   ", "lastName" -> "Doe")).apply("firstName")

      result.errors mustEqual Seq(FormError("firstName", "sa1.name.firstName.error.required"))
    }

    "must not bind when the last name is missing" in {
      val result = form.bind(Map("firstName" -> "Jane")).apply("lastName")

      result.errors mustEqual Seq(FormError("lastName", "sa1.name.lastName.error.required"))
    }

    "must not bind when the last name is blank" in {
      val result = form.bind(Map("firstName" -> "Jane", "lastName" -> "")).apply("lastName")

      result.errors mustEqual Seq(FormError("lastName", "sa1.name.lastName.error.required"))
    }

    "must not bind a first name longer than 100 characters" in {
      val result = form.bind(Map("firstName" -> ("a" * (maxLength + 1)), "lastName" -> "Doe")).apply("firstName")

      result.errors.map(_.message) mustEqual Seq("sa1.name.firstName.error.length")
    }

    "must not bind a last name longer than 100 characters" in {
      val result = form.bind(Map("firstName" -> "Jane", "lastName" -> ("a" * (maxLength + 1)))).apply("lastName")

      result.errors.map(_.message) mustEqual Seq("sa1.name.lastName.error.length")
    }

    "must not bind a first name containing invalid characters" in {
      val result = form.bind(Map("firstName" -> "Jane(", "lastName" -> "Doe")).apply("firstName")

      result.errors.map(_.message) mustEqual Seq("sa1.name.firstName.error.invalid")
    }

    "must not bind a last name containing invalid characters" in {
      val result = form.bind(Map("firstName" -> "Jane", "lastName" -> "Doe!")).apply("lastName")

      result.errors.map(_.message) mustEqual Seq("sa1.name.lastName.error.invalid")
    }
  }
}
