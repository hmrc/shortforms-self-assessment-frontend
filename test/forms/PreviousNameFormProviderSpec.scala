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

import base.SpecBase
import models.PreviousName
import play.api.data.FormError

import java.time.LocalDate

class PreviousNameFormProviderSpec extends SpecBase {

  private val msgs         = play.api.test.Helpers.stubMessages()
  private val formProvider = new PreviousNameFormProvider
  private val form         = formProvider()(msgs)

  private val maxLength = 100

  private val requiredError    = "sa1.enterPreviousLastName.error.required"
  private val lengthError      = "sa1.enterPreviousLastName.error.length"
  private val invalidNameError = "sa1.enterPreviousLastName.error.invalid"
  private val minimumDateError = "sa1.enterPreviousLastName.date.error.mustBeAfterMinimum"

  private val minimumDate = LocalDate.of(1753, 1, 3)
  private val validDate   = LocalDate.of(2020, 5, 10)

  private val validData: Map[String, String] = Map(
    "previousLastName" -> "John Doe"
  ) ++ dateData(validDate)

  private def dateData(date: LocalDate): Map[String, String] =
    Map(
      "lastNameChangeDate.day"   -> date.getDayOfMonth.toString,
      "lastNameChangeDate.month" -> date.getMonthValue.toString,
      "lastNameChangeDate.year"  -> date.getYear.toString
    )

  private def bindDate(date: LocalDate) =
    form.bind(validData ++ dateData(date))

  "PreviousNameFormProvider" - {

    "must bind valid data" in {
      val result = form.bind(validData)

      result.hasErrors mustBe false
      result.value.value mustBe PreviousName("John Doe", validDate)
    }

    "must trim whitespace from the previous last name" in {
      val result = form.bind(validData.updated("previousLastName", "  John Doe  "))

      result.value.value.previousLastName mustBe "John Doe"
    }

    "must reject a previous last name containing only whitespace" in {
      val result = form.bind(validData.updated("previousLastName", "   "))

      result.errors must contain(FormError("previousLastName", requiredError))
    }

    "must reject a previous last name containing numbers" in {
      val result = form.bind(validData.updated("previousLastName", "Smith123"))

      result.errors must contain(
        FormError("previousLastName", invalidNameError, Seq("^[A-Za-z\\-' ]+$"))
      )
    }

    "must reject unsupported characters in the previous last name" in {
      val result = form.bind(validData.updated("previousLastName", "Smith@Jones"))

      result.errors must contain(
        FormError("previousLastName", invalidNameError, Seq("^[A-Za-z\\-' ]+$"))
      )
    }

    "must accept apostrophes, hyphens and spaces in a previous last name" in {
      val result = form.bind(validData.updated("previousLastName", "O'Brien-Smith Jones"))

      result.hasErrors mustBe false
    }

    "must reject a previous last name longer than 100 characters" in {
      val result = form.bind(validData.updated("previousLastName", "A" * 101))

      result.errors must contain(
        FormError("previousLastName", lengthError, Seq(maxLength))
      )
    }

    "must accept a previous last name of exactly 100 characters" in {
      val result = form.bind(validData.updated("previousLastName", "A" * maxLength))

      result.errors.filter(_.key == "previousLastName") mustBe empty
    }

    "must accept the minimum allowed date" in {
      val result = bindDate(minimumDate)

      result.errors.filter(_.key == "lastNameChangeDate") mustBe empty
    }

    "must accept a date after the minimum allowed date" in {
      val result = bindDate(LocalDate.of(1753, 1, 4))

      result.errors.filter(_.key == "lastNameChangeDate") mustBe empty
    }

    "must reject a date before the minimum allowed date" in {
      val result = bindDate(minimumDate.minusDays(1))

      result.errors must contain(
        FormError(
          "lastNameChangeDate",
          minimumDateError,
          Seq("3 January 1753")
        )
      )
    }
  }
}
