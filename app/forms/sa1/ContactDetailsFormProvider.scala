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

  private val regexEmailUserNameRule         = "^([\\w-]+\\S?)*[\\w-]$"
  private val regexEmailDomainValidationRule = "^[a-zA-Z0-9]([a-zA-Z0-9]?-?[a-zA-Z0-9]+)*$"
  private val regexPhoneNumber               = "^[0-9-()+ ]{0,40}$"
  private val emailIdMaxLength               = 129
  private val emailPartsMaxLength            = 64
  private val phoneNumberMaxLength           = 40

  def apply(): Form[ContactDetails] = Form(
    mapping(
      "emailAddress" -> text("sa1.contactDetails.error.emailAddress.required")
        .verifying(
          firstError(
            maxLength(emailIdMaxLength, "sa1.contactDetails.error.emailAddress.length"),
            validateEmail("sa1.contactDetails.emailAddress.invalid")
          )
        ),
      "phoneNumber"  -> text("sa1.contactDetails.error.phoneNumber.required")
        .verifying(
          firstError(
            maxLength(phoneNumberMaxLength, "sa1.contactDetails.error.phoneNumber.length"),
            regexp(regexPhoneNumber, "sa1.contactDetails.phoneNumber.invalid")
          )
        )
    )(ContactDetails.apply)(x => Some((x.emailAddress, x.phoneNumber)))
  )

  private def validateEmail(errorKey: String): Constraint[String] = Constraint { email =>
    email.split('@').toList match
      case username :: domain :: Nil if isUsernameValid(username) && isDomainValid(domain) => Valid
      case _                                                                               => Invalid(errorKey)
  }

  private def isUsernameValid(username: String): Boolean = {
    val isUserNameSegmentLengthValid: Boolean = username.length <= emailPartsMaxLength
    val isEmailUserNameRegexValid: Boolean    = username.matches(regexEmailUserNameRule)

    isUserNameSegmentLengthValid && isEmailUserNameRegexValid
  }

  private def isDomainValid(domain: String): Boolean =
    val isDomainSegmentLengthValid: Boolean = domain.length <= emailPartsMaxLength
    val hasDotExtensionValid: Boolean       = domain.contains(".") && !domain.startsWith(".") && !domain.endsWith(".")
    val emailDomainParts                    = domain.split("\\.")
    val isEmailDomainPartsValid: Boolean    = emailDomainParts.nonEmpty &&
      emailDomainParts.forall(_.matches(regexEmailDomainValidationRule))

    isDomainSegmentLengthValid && hasDotExtensionValid && isEmailDomainPartsValid
}
