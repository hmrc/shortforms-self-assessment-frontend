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

import forms.behaviours.CheckboxFieldBehaviours
import models.sa1.WhyTaxReturn
import play.api.data.FormError

class WhyTaxReturnFormProviderSpec extends CheckboxFieldBehaviours {

  val requiredKey = "sa1.whyTaxReturn.error.required"

  val form = new WhyTaxReturnFormProvider()()

  ".value" - {

    val fieldName = "value"

    behave like checkboxField[WhyTaxReturn](
      form,
      fieldName,
      validValues = WhyTaxReturn.values,
      invalidError = FormError(s"$fieldName[0]", "error.invalid")
    )

    behave like mandatoryCheckboxField(
      form,
      fieldName,
      requiredKey
    )

    "bind multiple selected values" in {
      val data = Map(
        s"$fieldName[0]" -> WhyTaxReturn.CompanyDirector.toString,
        s"$fieldName[1]" -> WhyTaxReturn.Other.toString
      )

      val result = form.bind(data)

      result.get mustEqual Set(WhyTaxReturn.CompanyDirector, WhyTaxReturn.Other)
      result.errors mustBe empty
    }
  }
}
