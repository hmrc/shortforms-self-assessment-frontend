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
import javax.inject.Singleton

@Singleton
class DateOfBirthFormProvider extends Mappings {

  private val dateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")

  def apply()(implicit messages: Messages): Form[LocalDate] = {
    val maximumDate = LocalDate.now().minusYears(15).minusMonths(9)

    Form(
      "dateOfBirth" -> localDate(
        invalidKey = "sa1.dateOfBirth.date.error.invalid",
        allRequiredKey = "sa1.dateOfBirth.date.error.required",
        twoRequiredKey = "sa1.dateOfBirth.date.error.requiredTwo",
        requiredKey = "sa1.dateOfBirth.date.error.requiredOne",
        numericOnly = true
      ).verifying(
        maxDate(
          maximumDate,
          "sa1.dateOfBirth.date.error.mustBeInPast",
          maximumDate.format(dateFormatter)
        )
      )
    )
  }
}
