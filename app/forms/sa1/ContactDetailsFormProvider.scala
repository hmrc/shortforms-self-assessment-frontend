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
import models.sa1.ContactDetails
import play.api.data.Form
import play.api.data.Forms.*
import play.api.data.validation.{Constraint, Invalid, Valid}

import javax.inject.Inject

class ContactDetailsFormProvider @Inject() extends Mappings {

  private val regexEmailUserNameRule = "^[a-zA-Z0-9]([a-zA-Z0-9]?-?[a-zA-Z0-9]+)*$"
  private val regexEmailDomainValidationRule = "^([\\w-]+\\S?)*[\\w-]$"
  private val regexPhoneNumber = "^[0-9-()+ ]{0,40}$"
  private val emailIdMaxLength = 129
  private val phoneNumberMaxLength = 40


  def apply(): Form[ContactDetails] = Form(
    mapping(
      "emailAddress" -> text("sa1.contactDetails.error.emailAddress.required")
        .verifying(
          firstError(
            maxLength(emailIdMaxLength, "sa1.contactDetails.error.emailAddress.length"),
            validateEmail("sa1.contactDetails.emailAddress.invalid"))
        ),
      "phoneNumber" -> text("sa1.contactDetails.error.phoneNumber.required")
        .verifying(firstError(maxLength(phoneNumberMaxLength, "sa1.contactDetails.error.phoneNumber.length"),
          regexp(regexPhoneNumber, "sa1.contactDetails.phoneNumber.invalid")))
    )(ContactDetails.apply)(x => Some((x.emailAddress, x.phoneNumber)))
  )

  private def validateEmail(errorKey: String): Constraint[String] = Constraint {
    email =>
      if (!email.contains("@")) {
        Invalid(errorKey)
      } else {
        val parts = email.split("@")
        if (parts.length == 2) {
          val username = parts(0)
          val emailDomain = parts(1)
          val isUsernameLengthValid = username.length <= 64
          val isDomainSegmentLengthValid = emailDomain.length <= 64
          val hasDotExtension = emailDomain.contains(".") && !emailDomain.startsWith(".") && !emailDomain.endsWith(".")
          val emailDomainParts = emailDomain.split("\\.")

          val isUserNameValid = isUsernameLengthValid && username.matches(regexEmailUserNameRule)
          val isDomainValid = isDomainSegmentLengthValid && hasDotExtension && emailDomainParts.nonEmpty && emailDomainParts.forall(_.matches(regexEmailDomainValidationRule))
          if (isUserNameValid && isDomainValid) {
            Valid
          } else {
            Invalid(errorKey)
          }
        } else {
          Invalid(errorKey)
        }
      }
  }
}

