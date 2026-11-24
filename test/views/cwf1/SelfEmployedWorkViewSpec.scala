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
import forms.cwf1.SelfEmployedWorkFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.test.FakeRequest
import play.api.test.Helpers.stubMessages
import views.html.cwf1.SelfEmployedWorkView

class SelfEmployedWorkViewSpec extends SpecBase with GuiceOneAppPerSuite {

  private val msgs         = stubMessages()
  private val view         = app.injector.instanceOf[SelfEmployedWorkView]
  private val formProvider = new SelfEmployedWorkFormProvider()
  private val form         = formProvider()

  "SelfEmployedWorkView" - {
    "must render the title, heading" in {
      val doc = Jsoup.parse(view(form, userAnswersId, NormalMode)(FakeRequest(), msgs).body)

      doc.title() must include(msgs("cwf1.selfEmployedWork.title"))
      doc.select("h1").text() mustBe msgs("cwf1.selfEmployedWork.heading")
    }

    "must render self employed work input" in {
      val doc = Jsoup.parse(view(form, userAnswersId, NormalMode)(FakeRequest(), msgs).body)

      doc.select("input#value").size() mustBe 1
      doc.select("label[for=value]").text() mustBe msgs("cwf1.selfEmployedWork.heading")
    }

    "must pre-fill the inputs from the supplied form" in {
      val filledForm = form.fill("Software Developer")

      val doc = Jsoup.parse(view(filledForm, userAnswersId, NormalMode)(FakeRequest(), msgs).body)

      doc.select("input#value").attr("value") mustBe "Software Developer"
    }

    "must show an error summary and error messages when the form has errors" in {
      val invalidForm = form.bind(Map("value" -> ""))

      val doc = Jsoup.parse(view(invalidForm, userAnswersId, NormalMode)(FakeRequest(), msgs).body)

      doc.select(".govuk-error-summary").size() mustBe 1
      doc.select(".govuk-error-message").text() must include(msgs("cwf1.selfEmployedWork.error.required"))
    }
  }
}
