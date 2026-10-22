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

import forms.sa1.NoNinoReasonFormProvider
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.test.FakeRequest
import play.api.test.Helpers.stubMessages
import views.html.sa1.NoNinoReasonView

class NoNinoReasonViewSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite {

  private val journeyId    = "test-journey-id"
  private val msgs         = stubMessages()
  private val view         = app.injector.instanceOf[NoNinoReasonView]
  private val formProvider = app.injector.instanceOf[NoNinoReasonFormProvider]
  private val request      = FakeRequest()

  private val fieldId = "value"
  private val reason  = "No Nino"

  "NoNinoReasonView" should {

    "render the page title and heading" in {
      val form = formProvider()
      val doc  = Jsoup.parse(view(form, journeyId)(request, msgs).body)

      doc.title() must include(msgs("sa1.noNinoReason.title"))
      doc.select("h1").size() mustBe 1
      doc.select("h1").text() mustBe msgs("sa1.noNinoReason.heading")
    }

    "render the hint and character count textarea" in {
      val form = formProvider()
      val doc  = Jsoup.parse(view(form, journeyId)(request, msgs).body)

      doc.select(s"#$fieldId-hint").text() mustBe
        msgs("sa1.noNinoReason.hint")

      doc.select(s"textarea#$fieldId").size() mustBe 1
      doc.select(s"textarea#$fieldId[name='$fieldId']").size() mustBe 1

      doc.select(".govuk-character-count").size() mustBe 1
      doc.select(".govuk-character-count__message").text() must include("2000")
    }

    "render an error summary when the form has errors" in {
      val form = formProvider().bind(
        Map("value" -> "")
      )

      val doc = Jsoup.parse(view(form, journeyId)(request, msgs).body)

      doc.select(".govuk-error-summary").size() mustBe 1
      doc.select(".govuk-error-summary__title").text() mustBe
        msgs("error.summary.title")
    }

    "render the field error message when the form has errors" in {
      val form = formProvider().bind(
        Map("value" -> "")
      )

      val doc = Jsoup.parse(view(form, journeyId)(request, msgs).body)

      doc.select(".govuk-error-message").size() mustBe 1
      doc.select(".govuk-error-message").text() must include(
        msgs("sa1.noNinoReason.error.required")
      )
    }

    "render the reason when the form is populated" in {
      val form = formProvider().fill(reason)

      val doc = Jsoup.parse(view(form, journeyId)(request, msgs).body)

      doc.select(s"textarea#$fieldId").text() mustBe reason
    }

    "render the Continue button and correct form action" in {
      val form        = formProvider()
      val doc         = Jsoup.parse(view(form, journeyId)(request, msgs).body)
      val formElement = doc.select("form")

      formElement.size() mustBe 1
      formElement.attr("action") mustBe controllers.sa1.routes.NoNinoReasonController
        .onSubmit(journeyId, models.NormalMode)
        .url

      formElement.select("button[type=submit]").text() mustBe msgs("site.continue")
    }
  }
}
