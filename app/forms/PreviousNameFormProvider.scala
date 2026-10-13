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

import models.PreviousName
import play.api.data.Form
import play.api.data.Forms.*
import play.api.data.validation.{Constraint, Invalid, Valid}

import java.time.{LocalDate, DateTimeException}
import javax.inject.Inject
import javax.inject.Singleton
import scala.util.Try

@Singleton
class PreviousNameFormProvider @Inject()() {

  private val validDate: Constraint[PreviousName] =
    Constraint("constraints.previousName.date") { previousName =>
      Try(LocalDate.of(
        previousName.year,
        previousName.month,
        previousName.day
      )).toOption match {
        case Some(date) if !date.isAfter(LocalDate.now()) =>
          Valid

        case _ =>
          Invalid("previousName.date.error.invalid")
      }
    }

  def apply(): Form[PreviousName] =
    Form(
      mapping(
        "previousLastName" -> nonEmptyText
          .verifying(
            "previousName.lastName.error.required",
            _.trim.nonEmpty
          )
          .transform[String](_.trim, identity),

        "dateNameChanged" -> mapping(
          "day" -> number
            .verifying("previousName.date.error.required", _ >= 1),
          "month" -> number
            .verifying("previousName.date.error.required", _ >= 1),
          "year" -> number
            .verifying("previousName.date.error.required", _ >= 1)
        )((day, month, year) => (day, month, year))(
          date => Some((date._1, date._2, date._3))
        )
      )(
        (previousLastName, date) =>
          PreviousName(
            previousLastName = previousLastName,
            day = date._1,
            month = date._2,
            year = date._3
          )
      )(
        previousName =>
          Some((
            previousName.previousLastName,
            (previousName.day, previousName.month, previousName.year)
          ))
      ).verifying(validDate)
    )
}
