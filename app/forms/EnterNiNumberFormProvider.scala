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

import com.google.inject.{Inject, Singleton}
import forms.mappings.Mappings
import play.api.data.Form
import play.api.data.validation.{Constraint, Invalid, Valid}

@Singleton
class EnterNiNumberFormProvider @Inject() extends Mappings {
  private val maxLength   = 9
  private val RegexFormat = "^[A-Za-z]{2}[0-9]{6}[A-Za-z ]?$"

  def apply(actualNinoOpt: Option[String]): Form[String] =
    Form(
      "value" -> text("nino.error.required")
        .verifying(
          firstError(
            maxLengthNino(maxLength, "nino.error.length"),
            regexpNinO(RegexFormat, "nino.error.invalid"),
            givenNinoMatches(actualNinoOpt)
          )
        )
    )

  private inline def givenNinoMatches(actualNinoOpt: Option[String]) =
    Constraint { (givenNino: String) =>
      actualNinoOpt match
        case Some(actualNino) if !areNinosEqual(givenNino, actualNino) => Invalid("nino.error.mismatch")
        case _                                                         => Valid
    }

  private inline def areNinosEqual(nino1: String, nino2: String) =
    nino1.replace(" ", "") equalsIgnoreCase nino2.replace(" ", "")
}
