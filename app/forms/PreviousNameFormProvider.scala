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

package forms

import forms.mappings.Mappings
import models.{PreviousName}
import play.api.data.Form
import play.api.data.Forms.*
import play.api.data.validation.{Constraint, Invalid, Valid}
import play.api.i18n.Messages

import java.time.{Clock, DateTimeException, LocalDate}
import javax.inject.{Inject, Singleton}
import scala.util.Try

@Singleton
class PreviousNameFormProvider @Inject()(clock: Clock) extends  Mappings {
  
  def apply ()(implicit messages: Messages): Form[PreviousName] =
    Form(mapping(
      "previousLastName" -> text("sa1.enterPreviousLastName.error.required")
        .transform[String](_.trim, identity)
        .verifying(maxLength(100, "sa1.enterPreviousLastName.error.length"))
        .verifying(
          "sa1.enterPreviousLastName.error.required",
          _.matches("^[A-Za-z\\-' ]+$")
        ),
      "lastNameChangeDate" -> localDate(
        invalidKey = "sa1.enterPreviousLastName.date.error.invalid",
        allRequiredKey = "sa1.enterPreviousLastName.date.error.required",
        twoRequiredKey = "sa1.enterPreviousLastName.date.error.requiredTwo",
        requiredKey = "sa1.enterPreviousLastName.date.error.requiredOne",
        numericOnly = true
      ).verifying(
        maxDate(
          LocalDate.now(clock).minusDays(1),
          "sa1.enterPreviousLastName.date.error.future",
          Seq(
            LocalDate.now(clock).minusDays(1)
              .format(java.time.format.DateTimeFormatter.ofPattern("d MMMM yyyy"))
          ): _*
        )
      )
    )(PreviousName.apply)(answer => Some((answer.previousLastName, answer.lastNameChangeDate)))
  )
}
