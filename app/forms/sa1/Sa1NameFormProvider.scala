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
import models.sa1.Sa1Name
import play.api.data.{Form, Mapping}
import play.api.data.Forms.mapping

import javax.inject.Inject

class Sa1NameFormProvider @Inject() extends Mappings {

  private val nameMaxLength = 100
  private val nameRegex = "^[A-Za-z0-9'\\- ]+$"

  def apply(): Form[Sa1Name] =
    Form(
      mapping(
        "firstName" -> nameField("firstName"),
        "lastName" -> nameField("lastName")
      )((firstName, lastName) => Sa1Name(firstName, lastName))(name => Some((name.firstName, name.lastName)))
    )

  private def nameField(field: String): Mapping[String] =
    text(s"sa1.name.$field.error.required")
      .transform[String](_.trim, identity)
      .verifying(
        firstError(
          maxLength(nameMaxLength, s"sa1.name.$field.error.length"),
          regexp(nameRegex, s"sa1.name.$field.error.invalid")
        )
      )
}
