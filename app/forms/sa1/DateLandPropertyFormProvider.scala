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
import play.api.data.Forms.*
import play.api.i18n.Messages

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Singleton

@Singleton
class DateLandPropertyFormProvider extends Mappings {

  private val dateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")
  private val minimumDate   = LocalDate.of(1753, 1, 3)

  def apply()(implicit messages: Messages): Form[LocalDate] =
    Form(
      "dateLandPropertyUk" -> localDate(
        invalidKey = "sa1.dateLandPropertyUk.date.error.invalid",
        allRequiredKey = "sa1.dateLandPropertyUk.date.error.required",
        twoRequiredKey = "sa1.dateLandPropertyUk.date.error.requiredTwo",
        requiredKey = "sa1.dateLandPropertyUk.date.error.requiredOne",
        numericOnly = true
      ).verifying(
        minDate(
          minimumDate,
          "sa1.dateLandPropertyUk.date.error.mustBeAfterMinimum",
          minimumDate.format(dateFormatter)
        )
      )
    )
}
