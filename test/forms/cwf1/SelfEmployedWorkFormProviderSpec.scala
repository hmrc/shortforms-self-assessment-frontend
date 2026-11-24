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

class SelfEmployedWorkFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "cwf1.selfEmployedWork.error.required"
  val lengthKey   = "cwf1.selfEmployedWork.error.length"
  val invalidKey  = "cwf1.selfEmployedWork.error.invalid"
  val maxLength   = 100

  val form = new SelfEmployedWorkFormProvider()()

  ".value" - {

    val fieldName = "value"

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    "must bind valid data" in {

      val validData = List(
        "Director, Finance",
        "Editor-in-Chief",
        "Children's Nurse",
        "Author, Writer.",
        "\"Creative\" Director",
        "Vice-President, Sales",
        "Professor Emeritus.",
        "O'Connor, Solicitor"
      )

      validData.foreach { data =>
        val result = form.bind(Map(fieldName -> data))
        result.errors mustBe empty
      }
    }

    "must not bind invalid data" in {

      val invalidData = List(
        "Software_Developer",
        "Software@Developer",
        "Software#Developer",
        "Software$Developer",
        "Software%Developer",
        "Software&Developer",
        "Software*Developer",
        "Software+Developer"
      )

      invalidData.foreach { data =>
        val result = form.bind(Map(fieldName -> data))
        result.errors must contain(
          FormError(fieldName, invalidKey, Seq("^([A-Za-z0-9\\\"',.\\- ]){1,100}$"))
        )
      }
    }
  }
}
