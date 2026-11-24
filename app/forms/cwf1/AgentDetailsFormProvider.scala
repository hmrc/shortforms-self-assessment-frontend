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
import models.cwf1.AgentDetails
import play.api.data.Form
import play.api.data.Forms.{text as formText, *}

import javax.inject.Inject

class AgentDetailsFormProvider @Inject() extends Mappings {

  private val regexFullName = "^[A-Za-z0-9-' ]+$"

  def apply(): Form[AgentDetails] = Form(
    mapping(
      "fullName"        -> text("cwf1.agentDetails.error.fullName.required")
        .verifying(
          firstError(
            regexp(regexFullName, "cwf1.agentDetails.error.fullName.invalid"),
            maxLength(100, "cwf1.agentDetails.error.fullName.length")
          )
        ),
      "clientReference" -> optional(
        formText
          .verifying(maxLength(50, "cwf1.agentDetails.error.clientReference.length"))
      )
    )(AgentDetails.apply)(x => Some((x.fullName, x.clientReference)))
  )
}
