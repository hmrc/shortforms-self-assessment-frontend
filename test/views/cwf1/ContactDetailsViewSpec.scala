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

package views.cwf1

import base.SpecBase
import forms.cwf1.ContactDetailsFormProvider
import models.NormalMode
import models.cwf1.ContactDetails
import org.jsoup.Jsoup
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.test.FakeRequest
import play.api.test.Helpers.stubMessages
import views.html.cwf1.ContactDetailsView

class ContactDetailsViewSpec extends SpecBase with GuiceOneAppPerSuite {

  private val msgs         = stubMessages()
  private val view         = app.injector.instanceOf[ContactDetailsView]
  private val formProvider = new ContactDetailsFormProvider()
  private val form         = formProvider()
  private val journeyId    = "test-journey-id"

  "ContactDetailsView" - {
    "must render the title, heading" in {
      val doc = Jsoup.parse(view(form, journeyId, NormalMode)(FakeRequest(), msgs).body)

      doc.title() must include(msgs("cwf1.contactDetails.title"))
      doc.select("h1").text() mustBe msgs("cwf1.contactDetails.heading")
    }

    "must render email address and full name inputs" in {
      val doc = Jsoup.parse(view(form, journeyId, NormalMode)(FakeRequest(), msgs).body)

      doc.select("input#emailAddress").size() mustBe 1
      doc.select("input#phoneNumber").size() mustBe 1
      doc.select("label[for=emailAddress]").text() mustBe msgs("cwf1.contactDetails.emailAddress")
      doc.select("label[for=phoneNumber]").text() mustBe msgs("cwf1.contactDetails.phoneNumber")
    }

    "must pre-fill the inputs from the supplied form" in {
      val filledForm = form.fill(ContactDetails("example@test.com", "07878787878"))

      val doc = Jsoup.parse(view(filledForm, journeyId, NormalMode)(FakeRequest(), msgs).body)

      doc.select("input#emailAddress").attr("value") mustBe "example@test.com"
      doc.select("input#phoneNumber").attr("value") mustBe "07878787878"
    }

    "must show an error summary and error messages when the form has errors" in {
      val invalidForm = form.bind(Map("emailAddress" -> "", "phoneNumber" -> ""))

      val doc = Jsoup.parse(view(invalidForm, journeyId, NormalMode)(FakeRequest(), msgs).body)

      doc.select(".govuk-error-summary").size() mustBe 1
      doc.select(".govuk-error-message").text() must include(msgs("cwf1.contactDetails.error.emailAddress.required"))
      doc.select(".govuk-error-message").text() must include(msgs("cwf1.contactDetails.error.phoneNumber.required"))
    }
  }
}
