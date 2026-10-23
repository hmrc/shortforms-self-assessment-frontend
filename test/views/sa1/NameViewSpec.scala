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

package views.sa1

import base.SpecBase
import forms.sa1.NameFormProvider
import models.sa1.Name
import models.{CheckMode, NormalMode}
import org.jsoup.Jsoup
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.data.Form
import play.api.test.FakeRequest
import views.html.sa1.NameView

class NameViewSpec extends SpecBase with GuiceOneAppPerSuite {
  private val journeyId = "test-journey-id"

  private lazy val msgs     = messages(app)
  lazy val view: NameView   = app.injector.instanceOf[NameView]
  lazy val form: Form[Name] = app.injector.instanceOf[NameFormProvider].apply()

  "NameView" - {

    "must render the title, heading" in {
      val doc = Jsoup.parse(view(form, journeyId, NormalMode)(FakeRequest(), msgs).body)

      doc.title() must include(msgs("sa1.name.title"))
      doc.select("h1").text() mustBe msgs("sa1.name.heading")
    }

    "must render first and last name inputs" in {
      val doc = Jsoup.parse(view(form, journeyId, NormalMode)(FakeRequest(), msgs).body)

      doc.select("input#firstName").size() mustBe 1
      doc.select("input#lastName").size() mustBe 1
      doc.select("label[for=firstName]").text() mustBe msgs("sa1.name.firstName")
      doc.select("label[for=lastName]").text() mustBe msgs("sa1.name.lastName")
    }

    "must pre-fill the inputs from the supplied form" in {
      val filledForm = form.fill(Name("Jane", "Doe"))

      val doc = Jsoup.parse(view(filledForm, journeyId, NormalMode)(FakeRequest(), msgs).body)

      doc.select("input#firstName").attr("value") mustBe "Jane"
      doc.select("input#lastName").attr("value") mustBe "Doe"
    }

    "must post to the normal route in normal mode" in {
      val doc = Jsoup.parse(view(form, journeyId, NormalMode)(FakeRequest(), msgs).body)

      doc.select("form").attr("action") mustBe
        controllers.sa1.routes.NameController.onSubmit(journeyId, NormalMode).url
    }

    "must post to the change route in check mode" in {
      val doc = Jsoup.parse(view(form, journeyId, CheckMode)(FakeRequest(), msgs).body)

      doc.select("form").attr("action") mustBe
        controllers.sa1.routes.NameController.onSubmit(journeyId, CheckMode).url
    }

    "must link back to the SA1 start page" in {
      val doc = Jsoup.parse(view(form, journeyId, NormalMode)(FakeRequest(), msgs).body)

      doc.select("a.govuk-back-link").attr("href") mustBe
        controllers.sa1.routes.StartController.onPageLoad().url
    }

    "must show an error summary and error messages when the form has errors" in {
      val invalidForm = form.bind(Map("firstName" -> "", "lastName" -> ""))

      val doc = Jsoup.parse(view(invalidForm, journeyId, NormalMode)(FakeRequest(), msgs).body)

      doc.select(".govuk-error-summary").size() mustBe 1
      doc.select(".govuk-error-message").text() must include(msgs("sa1.name.firstName.error.required"))
      doc.select(".govuk-error-message").text() must include(msgs("sa1.name.lastName.error.required"))
    }
  }
}
