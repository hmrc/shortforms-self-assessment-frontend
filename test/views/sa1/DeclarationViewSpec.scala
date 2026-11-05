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
import forms.sa1.DeclarationFormProvider
import org.jsoup.Jsoup
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.test.FakeRequest
import views.html.sa1.DeclarationView

class DeclarationViewSpec extends SpecBase with GuiceOneAppPerSuite {

  private lazy val msgs = messages(app)
  private lazy val view = app.injector.instanceOf[DeclarationView]
  private lazy val form = app.injector.instanceOf[DeclarationFormProvider].apply()

  "DeclarationView" - {

    "render the page title, heading, statement, name field and button" in {
      val doc = Jsoup.parse(view(form, userAnswersId)(FakeRequest(), msgs).body)

      doc.title() mustBe
        s"${msgs("sa1.declaration.title")} - ${msgs("service.sa1.name")} - ${msgs("site.govuk")}"
      doc.select("h1").text() mustBe msgs("sa1.declaration.heading")
      doc.select("p.govuk-body").text() mustBe msgs("sa1.declaration.statement")
      doc.select("label[for=value]").text() mustBe msgs("sa1.declaration.name")
      doc.select("input#value").hasAttr("maxlength") mustBe false
      doc.select("input#value").attr("autocomplete") mustBe "off"
      doc.select("input#value").hasClass("govuk-input--width-30") mustBe true
      doc.select("button").text() must include(msgs("sa1.declaration.confirmAndSubmit"))
      doc.select("button .govuk-visually-hidden").text() mustBe
        msgs("sa1.declaration.confirmAndSubmit.hidden")
    }

    "render a single back link supplied by the layout" in {
      val doc = Jsoup.parse(view(form, userAnswersId)(FakeRequest(), msgs).body)

      doc.select("a.govuk-back-link").size() mustBe 1
      doc.select("a.govuk-back-link").text() mustBe msgs("site.back")
    }

    "show the error summary and field error when the name is too long" in {
      val invalidForm = form.bind(Map("value" -> "A" * 101))
      val doc         = Jsoup.parse(view(invalidForm, userAnswersId)(FakeRequest(), msgs).body)

      doc.select(".govuk-error-summary").size() mustBe 1
      doc.select(".govuk-error-summary__title").text() mustBe msgs("error.summary.title")
      doc.select(".govuk-error-message").text() must include(msgs("sa1.declaration.name.error.length"))
      doc.select(".govuk-error-summary__list a").attr("href") mustBe "#value"
      doc.select("input#value").attr("value") mustBe "A" * 101
    }
  }
}
