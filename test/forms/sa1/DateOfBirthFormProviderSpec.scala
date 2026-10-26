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

import forms.behaviours.DateBehaviours
import play.api.data.FormError
import play.api.test.Helpers.stubMessages

import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DateOfBirthFormProviderSpec extends DateBehaviours {

  private val formProvider  = new DateOfBirthFormProvider()
  private val msgs          = stubMessages()
  private val maximumDate   = LocalDate.now().minusYears(15).minusMonths(9)
  private val dateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")
  private val form          = formProvider()(msgs)

  private val maximumDateError = FormError(
    "dateOfBirth",
    "sa1.dateOfBirth.date.error.mustBeInPast",
    Seq(maximumDate.format(dateFormatter))
  )

  "DateOfBirthFormProvider" - {

    "dateOfBirth" - {

      dateField(
        form,
        "dateOfBirth",
        datesBetween(LocalDate.of(1900, 1, 1), maximumDate)
      )

      dateFieldWithMax(
        form,
        "dateOfBirth",
        maximumDate,
        maximumDateError
      )

      mandatoryDateField(
        form,
        "dateOfBirth",
        "sa1.dateOfBirth.date.error.required"
      )

      "accept the maximum date" in {
        val result = form.bind(dateData(maximumDate))

        result.value.value mustBe maximumDate
        result.errors mustBe empty
      }
    }
  }

  private def dateData(date: LocalDate): Map[String, String] =
    Map(
      "dateOfBirth.day"   -> date.getDayOfMonth.toString,
      "dateOfBirth.month" -> date.getMonthValue.toString,
      "dateOfBirth.year"  -> date.getYear.toString
    )
}
