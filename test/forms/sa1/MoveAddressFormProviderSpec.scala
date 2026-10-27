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

class MoveAddressFormProviderSpec extends DateBehaviours {

  private val formProvider  = new MoveAddressFormProvider()
  private val msgs          = stubMessages()
  private val maximumDate   = LocalDate.now()
  private val minimumDate   = maximumDate.minusYears(120)
  private val dateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")
  private val form          = formProvider()(msgs)

  private val maximumDateError = FormError(
    "moveAddressDate",
    "sa1.moveAddress.date.error.mustBeInPast",
    Seq(maximumDate.format(dateFormatter))
  )

  private val minimumDateError = FormError(
    "moveAddressDate",
    "sa1.moveAddress.date.error.mustBeOnOrAfter",
    Seq(minimumDate.format(dateFormatter))
  )

  "MoveAddressFormProvider" - {

    "moveAddress" - {

      dateField(
        form,
        "moveAddressDate",
        datesBetween(minimumDate, maximumDate)
      )

      dateFieldWithMax(
        form,
        "moveAddressDate",
        maximumDate,
        maximumDateError
      )

      mandatoryDateField(
        form,
        "moveAddressDate",
        "sa1.moveAddress.date.error.required"
      )

      "accept the maximum date" in {
        val result = form.bind(dateData(maximumDate))

        result.value.value mustBe maximumDate
        result.errors mustBe empty
      }

      "accept the minimum date" in {
        val result = form.bind(dateData(minimumDate))

        result.value.value mustBe minimumDate
        result.errors mustBe empty
      }

      "reject a date before the minimum date" in {
        val dateBeforeMinimum = minimumDate.minusDays(1)
        val result            = form.bind(dateData(dateBeforeMinimum))

        result.errors must contain only minimumDateError
      }
    }
  }

  private def dateData(date: LocalDate): Map[String, String] =
    Map(
      "moveAddressDate.day"   -> date.getDayOfMonth.toString,
      "moveAddressDate.month" -> date.getMonthValue.toString,
      "moveAddressDate.year"  -> date.getYear.toString
    )
}
