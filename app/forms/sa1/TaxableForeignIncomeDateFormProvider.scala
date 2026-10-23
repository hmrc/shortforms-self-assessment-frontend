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

import forms.mappings.Mappings
import play.api.data.Form
import play.api.i18n.Messages

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class TaxableForeignIncomeDateFormProvider @Inject() extends Mappings {
  private val dateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")

  def apply()(implicit messages: Messages): Form[LocalDate] =
    val minimumDate = LocalDate.of(1753, 1, 3)
    Form(
      "value" -> localDate(
        invalidKey = "sa1.taxableForeignIncomeDate.date.error.invalid",
        allRequiredKey = "sa1.taxableForeignIncomeDate.date.error.required",
        twoRequiredKey = "sa1.taxableForeignIncomeDate.date.error.required.two",
        requiredKey = "sa1.taxableForeignIncomeDate.date.error.requiredOne",
        numericOnly = true
      ).verifying(
        minDate(
          minimumDate,
          "sa1.taxableForeignIncomeDate.date.error.mustBeAfterMinimum",
          minimumDate.format(dateFormatter)
        )
      )
    )
}
