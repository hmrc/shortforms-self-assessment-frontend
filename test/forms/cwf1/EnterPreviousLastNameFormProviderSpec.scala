package forms.cwf1

import forms.behaviours.StringFieldBehaviours
import forms.cwf1.EnterPreviousLastNameFormProvider
import play.api.data.FormError

class EnterPreviousLastNameFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "enterPreviousLastName.error.required"
  val lengthKey = "enterPreviousLastName.error.length"
  val maxLength = 100

  val form = new EnterPreviousLastNameFormProvider()()

  ".value" - {

    val fieldName = "value"

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

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
