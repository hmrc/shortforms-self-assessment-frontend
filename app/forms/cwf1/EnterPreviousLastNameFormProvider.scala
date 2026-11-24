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

import forms.mappings.Mappings
import play.api.data.Form

import javax.inject.Inject

class EnterPreviousLastNameFormProvider @Inject() extends Mappings {

  private val maxLength             = 100
  private val previousLastNameRegex = "^[A-Za-z-' ]{1,100}$"

  def apply(): Form[String] =
    Form(
      "value" -> text("cwf1.enterPreviousLastName.error.required")
        .transform[String](_.trim, identity)
        .verifying(
          firstError(
            maxLength(maxLength, "cwf1.enterPreviousLastName.error.length"),
            regexp(previousLastNameRegex, "cwf1.enterPreviousLastName.error.invalid")
          )
        )
    )
}
