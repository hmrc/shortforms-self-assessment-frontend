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

import base.SpecBase
import models.sa1.OtherReason
import play.api.data.FormError
import play.api.test.Helpers.stubMessages

import java.time.LocalDate

class OtherReasonFormProviderSpec extends SpecBase {

  private val msgs         = stubMessages()
  private val formProvider = new OtherReasonFormProvider
  private val form         = formProvider()(msgs)

  private val maxLength     = 2000
  private val requiredError = "sa1.otherReason.error.required"

  private val minimumDate = LocalDate.of(1753, 1, 3)
  private val validDate   = LocalDate.of(2020, 5, 10)

  private val validData: Map[String, String] =
    Map("reason" -> "test reason") ++ dateData(validDate)

  private def dateData(date: LocalDate): Map[String, String] =
    Map(
      "reasonDate.day"   -> date.getDayOfMonth.toString,
      "reasonDate.month" -> date.getMonthValue.toString,
      "reasonDate.year"  -> date.getYear.toString
    )

  private def bindDate(date: LocalDate) =
    form.bind(validData ++ dateData(date))

  "OtherReasonFormProvider" - {

    "must bind valid data" in {
      val result = form.bind(validData)

      result.hasErrors mustBe false
      result.value.value mustBe OtherReason("test reason", validDate)
    }

    "must reject a reason containing only whitespace" in {
      val result = form.bind(validData.updated("reason", "   "))

      result.errors must contain(FormError("reason", requiredError))
    }

    "must reject a missing reason" in {
      val result = form.bind(validData - "reason")

      result.errors must contain(FormError("reason", requiredError))
    }

    "must reject a reason longer than 2000 characters" in {
      val result = form.bind(validData.updated("reason", "A" * (maxLength + 1)))

      result.errors must contain(
        FormError("reason", "sa1.otherReason.reason.error.length", Seq(maxLength))
      )
    }

    "must accept a reason of exactly 2000 characters" in {
      val result = form.bind(validData.updated("reason", "A" * maxLength))

      result.errors.filter(_.key == "reason") mustBe empty
    }

    "must accept the minimum allowed date" in {
      val result = bindDate(minimumDate)

      result.errors.filter(_.key == "reasonDate") mustBe empty
    }

    "must accept a date after the minimum allowed date" in {
      val result = bindDate(minimumDate.plusDays(1))

      result.errors.filter(_.key == "reasonDate") mustBe empty
    }

    "must reject a date before the minimum allowed date" in {
      val result = bindDate(minimumDate.minusDays(1))

      result.errors must contain(
        FormError("reasonDate", "sa1.otherReason.date.error.mustBeAfterMinimum", Seq("3 January 1753"))
      )
    }

    "must reject an invalid calendar date" in {
      val result = form.bind(
        validData ++ Map(
          "reasonDate.day"   -> "31",
          "reasonDate.month" -> "2",
          "reasonDate.year"  -> "2000"
        )
      )

      result.errors must contain(FormError("reasonDate", "sa1.otherReason.date.error.invalid"))
    }

    "must reject a date when all date components are missing" in {
      val result = form.bind(
        validData -- Seq(
          "reasonDate.day",
          "reasonDate.month",
          "reasonDate.year"
        )
      )

      result.errors must contain(FormError("reasonDate", "sa1.otherReason.date.error.required"))
    }

    "must reject a date when only one component is missing" in {
      val result = form.bind(validData - "reasonDate.day")

      result.errors must contain(
        FormError("reasonDate", "sa1.otherReason.date.error.requiredOne", Seq("date.error.day"))
      )
    }

    "must reject a date when two components are missing" in {
      val result = form.bind(
        validData -- Seq(
          "reasonDate.day",
          "reasonDate.month"
        )
      )

      result.errors must contain(
        FormError("reasonDate", "sa1.otherReason.date.error.requiredTwo", Seq("date.error.day", "date.error.month"))
      )
    }
  }
}
