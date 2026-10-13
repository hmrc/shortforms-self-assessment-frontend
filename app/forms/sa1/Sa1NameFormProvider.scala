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
import models.UserName
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.{Constraint, Invalid, Valid}

import javax.inject.Inject

class Sa1NameFormProvider @Inject() extends Mappings {

  private val namePattern = """^[A-Za-z0-9\s'-]*$"""

  private def validCharacters(fieldName: String): Constraint[String] =
    Constraint[String] { input =>
      if (input.matches(namePattern)) Valid
      else Invalid(s"sa1.sa1Name.error.$fieldName.invalidCharacters")
    }

  def apply(): Form[UserName] =
    Form(
      mapping(
        "firstName" -> text("sa1.sa1Name.error.firstName.required")
          .verifying(maxLength(100, "sa1.sa1Name.error.firstName.length"))
          .verifying(validCharacters("firstName")),
        "lastName"  -> text("sa1.sa1Name.error.lastName.required")
          .verifying(maxLength(100, "sa1.sa1Name.error.lastName.length"))
          .verifying(validCharacters("lastName"))
      )(UserName.apply)(u => Some((u.firstName, u.lastName)))
    )
}
