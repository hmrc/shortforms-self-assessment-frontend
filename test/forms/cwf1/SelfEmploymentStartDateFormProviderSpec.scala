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

import forms.behaviours.DateBehaviours
import play.api.data.FormError
import play.api.test.Helpers.stubMessages

import java.time.LocalDate

class SelfEmploymentStartDateFormProviderSpec extends DateBehaviours {

  private val formProvider = new SelfEmploymentStartDateFormProvider()
  private val msgs         = stubMessages()
  private val minimumDate  = LocalDate.of(1753, 1, 3)

  private val form = formProvider()(msgs)

  private val minimumDateError = FormError(
    "selfEmploymentStartDate",
    "cwf1.selfEmploymentStartDate.date.error.mustBeAfterMinimum"
  )

  "SelfEmploymentStartDateFormProvider" - {

    "selfEmploymentStartDate" - {

      mandatoryDateField(
        form,
        "selfEmploymentStartDate",
        "cwf1.selfEmploymentStartDate.date.error.required"
      )

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
      "selfEmploymentStartDate.day"   -> date.getDayOfMonth.toString,
      "selfEmploymentStartDate.month" -> date.getMonthValue.toString,
      "selfEmploymentStartDate.year"  -> date.getYear.toString
    )
}
