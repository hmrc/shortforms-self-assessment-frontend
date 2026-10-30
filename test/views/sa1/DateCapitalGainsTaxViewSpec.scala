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
import forms.sa1.DateCapitalGainsTaxFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.test.FakeRequest
import play.api.test.Helpers.stubMessages
import views.html.sa1.DateCapitalGainsTaxView

import java.time.LocalDate

class DateCapitalGainsTaxViewSpec extends SpecBase with GuiceOneAppPerSuite {

  private val msgs                        = stubMessages()
  private val dateCapitalGainsTaxProvider = new DateCapitalGainsTaxFormProvider()
  private val form                        = dateCapitalGainsTaxProvider()(msgs)
  val view                                = app.injector.instanceOf[DateCapitalGainsTaxView]
  val request                             = FakeRequest()

  "DateCapitalGainsTaxView" - {
    "must render the page title and main heading" in {
      val doc =
        Jsoup.parse(
          view(userAnswersId, form, NormalMode)(request, msgs).body
        )

      doc.title() must include(msgs("sa1.dateCapitalGainsTax.title"))

      doc.select("h1").size() mustEqual 1
      doc.select("h1").text() mustEqual msgs("sa1.dateCapitalGainsTax.heading")
    }

    "must render the date hint and input fields" in {
      val doc = Jsoup.parse(view(userAnswersId, form, NormalMode)(request, msgs).body)

      doc.select("#value-hint").text() mustBe
        msgs("sa1.dateCapitalGainsTax.hint.line1") + " " + msgs("sa1.dateCapitalGainsTax.hint.line2")

      doc.select("input[id='value.day']").size() mustBe 1
      doc.select("input[id='value.month']").size() mustBe 1
      doc.select("input[id='value.year']").size() mustBe 1
    }

    "must render an error summary when the form has errors" in {
      val invalidForm = form.bind(
        Map(
          "value.day"   -> "",
          "value.month" -> "",
          "value.year"  -> ""
        )
      )

      val doc = Jsoup.parse(view(userAnswersId, invalidForm, NormalMode)(request, msgs).body)

      doc.select(".govuk-error-summary").size() mustBe 1
      doc.select(".govuk-error-summary__title").text() mustBe
        msgs("error.summary.title")
    }

    "must render the date when the form is populated" in {
      val date          = LocalDate.of(2000, 10, 26)
      val populatedForm = form.fill(date)

      val doc = Jsoup.parse(view(userAnswersId, populatedForm, NormalMode)(request, msgs).body)

      doc.select("input[id='value.day']").attr("value") mustBe "26"
      doc.select("input[id='value.month']").attr("value") mustBe "10"
      doc.select("input[id='value.year']").attr("value") mustBe "2000"
    }

    "must render the Continue button and correct form action" in {
      val doc         = Jsoup.parse(view(userAnswersId, form, NormalMode)(request, msgs).body)
      val formElement = doc.select("form")

      formElement.size() mustBe 1
      formElement
        .attr("action") mustBe controllers.sa1.routes.DateCapitalGainsTaxController
        .onSubmit(userAnswersId, NormalMode)
        .url

      formElement.select("button[type=submit]").text() mustBe msgs("site.continue")
    }
  }

}
