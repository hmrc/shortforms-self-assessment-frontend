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

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class AgentDetailsFormProviderSpec extends StringFieldBehaviours {

  val form = new AgentDetailsFormProvider()()

  ".fullName" - {

    val fieldName   = "fullName"
    val requiredKey = "cwf1.agentDetails.error.fullName.required"
    val lengthKey   = "cwf1.agentDetails.error.fullName.length"
    val invalidKey  = "cwf1.agentDetails.error.fullName.invalid"

    "bind valid names" in {
      Seq(
        "John Smith",
        "John-Smith",
        "O'Connor",
        "A1B2C3"
      ).foreach { name =>
        form
          .bind(
            Map(
              "fullName"        -> name,
              "clientReference" -> "ABC123"
            )
          )
          .errors mustBe empty
      }
    }

    "reject names longer than 100 characters" in {
      val longName = "A" * 101

      val result = form.bind(
        Map(
          "fullName"        -> longName,
          "clientReference" -> "ABC123"
        )
      )

      result.errors must contain(
        FormError("fullName", lengthKey, Seq(100))
      )
    }

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    "must reject invalid name formats" in {
      val invalidFullNames = Seq(
        "Hello #world",
        "Price: $50",
        "A < B",
        "File \\ path",
        "José",
        "\"Line1\\nLine2\""
      )

      invalidFullNames.foreach { invalidName =>
        val result = form.bind(Map(fieldName -> invalidName))
        result.errors must contain(
          FormError(fieldName, invalidKey, Seq("^[A-Za-z0-9-' ]+$"))
        )
      }
    }
  }

  ".clientReference" - {

    val fieldName = "clientReference"
    val lengthKey = "cwf1.agentDetails.error.clientReference.length"
    val maxLength = 50

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )
  }
}
