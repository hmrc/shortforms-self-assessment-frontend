package forms.cwf1

import forms.mappings.Mappings
import play.api.data.Form

import javax.inject.Inject

class EnterPreviousLastNameFormProvider @Inject() extends Mappings {

  def apply(): Form[String] =
    Form(
      "value" -> text("enterPreviousLastName.error.required")
        .verifying(maxLength(100, "enterPreviousLastName.error.length"))
    )
}
