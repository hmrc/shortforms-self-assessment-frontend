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
import play.api.data.Forms.text as formText

import javax.inject.Inject

class DeclarationFormProvider @Inject() extends Mappings {

  private val nameMaxLength = 100

  def apply(): Form[String] =
    Form(
      "value" -> formText.verifying(
        maxLength(nameMaxLength, "sa1.declaration.name.error.length")
      )
    )
}
