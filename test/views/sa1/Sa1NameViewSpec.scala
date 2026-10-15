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
import forms.sa1.Sa1NameFormProvider
import models.sa1.Sa1Name
import org.jsoup.Jsoup
import play.api.test.FakeRequest
import play.api.test.Helpers.running
import views.html.sa1.Sa1NameView

class Sa1NameViewSpec extends SpecBase {

  private val journeyId = "test-journey-id"

  "Sa1Name view" - {

    "must render the title, caption and heading" in {
      val application = applicationBuilder().build()

      running(application) {
        val view = application.injector.instanceOf[Sa1NameView]
        val msgs = messages(application)
        val form = application.injector.instanceOf[Sa1NameFormProvider].apply()

        val doc = Jsoup.parse(view(form, journeyId)(FakeRequest(), msgs).body)

        doc.title() must include(msgs("sa1.name.title"))
        doc.select("span.govuk-caption-l").text() mustBe msgs("sa1.name.caption")
        doc.select("h1").text() mustBe msgs("sa1.name.heading")
      }
    }

    "must render first and last name inputs" in {
      val application = applicationBuilder().build()

      running(application) {
        val view = application.injector.instanceOf[Sa1NameView]
        val msgs = messages(application)
        val form = application.injector.instanceOf[Sa1NameFormProvider].apply()

        val doc = Jsoup.parse(view(form, journeyId)(FakeRequest(), msgs).body)

        doc.select("input#firstName").size() mustBe 1
        doc.select("input#lastName").size() mustBe 1
        doc.select("label[for=firstName]").text() mustBe msgs("sa1.name.firstName")
        doc.select("label[for=lastName]").text() mustBe msgs("sa1.name.lastName")
      }
    }

    "must pre-fill the inputs from the supplied form" in {
      val application = applicationBuilder().build()

      running(application) {
        val view = application.injector.instanceOf[Sa1NameView]
        val msgs = messages(application)
        val form = application.injector.instanceOf[Sa1NameFormProvider].apply().fill(Sa1Name("Jane", "Doe"))

        val doc = Jsoup.parse(view(form, journeyId)(FakeRequest(), msgs).body)

        doc.select("input#firstName").attr("value") mustBe "Jane"
        doc.select("input#lastName").attr("value") mustBe "Doe"
      }
    }

    "must link back to the SA1 start page" in {
      val application = applicationBuilder().build()

      running(application) {
        val view = application.injector.instanceOf[Sa1NameView]
        val msgs = messages(application)
        val form = application.injector.instanceOf[Sa1NameFormProvider].apply()

        val doc = Jsoup.parse(view(form, journeyId)(FakeRequest(), msgs).body)

        doc.select("a.govuk-back-link").attr("href") mustBe
          controllers.sa1.routes.StartController.onPageLoad().url
      }
    }

    "must show an error summary and error messages when the form has errors" in {
      val application = applicationBuilder().build()

      running(application) {
        val view = application.injector.instanceOf[Sa1NameView]
        val msgs = messages(application)
        val form = application.injector
          .instanceOf[Sa1NameFormProvider]
          .apply()
          .bind(Map("firstName" -> "", "lastName" -> ""))

        val doc = Jsoup.parse(view(form, journeyId)(FakeRequest(), msgs).body)

        doc.select(".govuk-error-summary").size() mustBe 1
        doc.select(".govuk-error-message").text() must include(msgs("sa1.name.firstName.error.required"))
        doc.select(".govuk-error-message").text() must include(msgs("sa1.name.lastName.error.required"))
      }
    }
  }
}
