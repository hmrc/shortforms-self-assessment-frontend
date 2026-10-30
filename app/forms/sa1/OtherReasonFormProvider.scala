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
import models.sa1.OtherReason
import play.api.data.Form
import play.api.data.Forms.*
import play.api.i18n.Messages

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Singleton

@Singleton
class OtherReasonFormProvider extends Mappings {

  private val minimumDate = LocalDate.of(1753, 1, 3)

  def apply()(implicit messages: Messages): Form[OtherReason] =
    Form(
      mapping(
        "reason"     -> text("sa1.otherReason.error.required")
          .verifying(
            maxLength(2000, "sa1.otherReason.reason.error.length")
          ),
        "reasonDate" -> localDate(
          invalidKey = "sa1.otherReason.date.error.invalid",
          allRequiredKey = "sa1.otherReason.date.error.required",
          twoRequiredKey = "sa1.otherReason.date.error.requiredTwo",
          requiredKey = "sa1.otherReason.date.error.requiredOne",
          numericOnly = true
        ).verifying(
          minDate(
            minimumDate,
            "sa1.otherReason.date.error.mustBeAfterMinimum",
            minimumDate.format(DateTimeFormatter.ofPattern("d MMMM yyyy"))
          )
        )
      )(OtherReason.apply)(answer => Some((answer.reason, answer.reasonDate)))
    )
}
