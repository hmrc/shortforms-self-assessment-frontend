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
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages

import java.time.{LocalDate, ZoneOffset}

class UntaxedIncomeNotInPayeStartDateFormProviderSpec extends DateBehaviours {

  private implicit val messages: Messages = stubMessages()
  private val form                        = new UntaxedIncomeNotInPayeStartDateFormProvider()()

  private val minDateAllowed: LocalDate = LocalDate.of(1753, 1, 3)

  ".value" - {

    val validData = datesBetween(
      min = LocalDate.of(2000, 1, 1),
      max = LocalDate.now(ZoneOffset.UTC)
    )

    behave like dateField(form, "value", validData)

    behave like dateFieldWithMin(
      form = form,
      key = "value",
      min = minDateAllowed,
      formError = FormError("value", "sa1.untaxedIncomeNotInPayeStartDate.date.range.min")
    )

    behave like mandatoryDateField(form, "value", "sa1.untaxedIncomeNotInPayeStartDate.error.required.all")

    "must unbind a valid date" in {
      val date = LocalDate.of(2000, 5, 15)

      val filledForm = form.fill(date)

      filledForm("value.day").value.value mustEqual "15"
      filledForm("value.month").value.value mustEqual "5"
      filledForm("value.year").value.value mustEqual "2000"
    }
  }
}
