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

class TrustSettlementDateFormProviderSpec extends DateBehaviours {

  private val formProvider  = new TrustSettlementDateFormProvider()
  private val msgs          = stubMessages()
  private val minimumDate   = LocalDate.of(1753, 1, 3)
  private val dateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")
  private val form          = formProvider()(msgs)

  private val minimumDateError = FormError(
    "trustSettlementDate",
    "sa1.trustSettlementDate.error.mustBeAfterMinimum",
    Seq(minimumDate.format(dateFormatter))
  )

  "TrustSettlementDateFormProvider" - {

    "trustSettlementDate" - {

      dateField(
        form,
        "trustSettlementDate",
        datesBetween(minimumDate, LocalDate.now())
      )

      mandatoryDateField(
        form,
        "trustSettlementDate",
        "sa1.trustSettlementDate.error.required"
      )

      "must reject a date before the minimum date" in {
        val result = form.bind(dateData(minimumDate.minusDays(1)))

        result.errors must contain(minimumDateError)
      }

      "must accept the minimum date" in {
        val result = form.bind(dateData(minimumDate))

        result.value.value mustBe minimumDate
        result.errors mustBe empty
      }

      "must accept a date after the minimum date" in {
        val date   = minimumDate.plusDays(1)
        val result = form.bind(dateData(date))

        result.value.value mustBe date
        result.errors mustBe empty
      }
    }
  }

  private def dateData(date: LocalDate): Map[String, String] =
    Map(
      "trustSettlementDate.day"   -> date.getDayOfMonth.toString,
      "trustSettlementDate.month" -> date.getMonthValue.toString,
      "trustSettlementDate.year"  -> date.getYear.toString
    )
}
