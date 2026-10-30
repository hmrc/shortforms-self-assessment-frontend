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
import forms.sa1.OtherReasonFormProvider
import models.sa1.OtherReason
import org.jsoup.Jsoup
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.test.FakeRequest
import play.api.test.Helpers.stubMessages
import views.html.sa1.OtherReasonView

import java.time.LocalDate

class OtherReasonViewSpec extends SpecBase with GuiceOneAppPerSuite {

  private val msgs         = stubMessages()
  private val view         = app.injector.instanceOf[OtherReasonView]
  private val formProvider = app.injector.instanceOf[OtherReasonFormProvider]
  private val request      = FakeRequest()

  "OtherReasonView" - {

    "render the page title and heading" in {
      val form = formProvider()(msgs)
      val doc  = Jsoup.parse(view(form, userAnswersId)(request, msgs).body)

      doc.title() must include(msgs("sa1.otherReason.title"))
      doc.select("h1").text() mustBe msgs("sa1.otherReason.heading")
    }

    "render the reason label and character count" in {
      val form = formProvider()(msgs)
      val doc  = Jsoup.parse(view(form, userAnswersId)(request, msgs).body)

      doc.select("label[for=reason]").text() mustBe msgs("sa1.otherReason.reason.label")

      doc.select("textarea#reason").size() mustBe 1
      doc.select("#reason-info").size() mustBe 1
    }

    "render the date hint and input fields" in {
      val form = formProvider()(msgs)
      val doc  = Jsoup.parse(view(form, userAnswersId)(request, msgs).body)

      doc.select("#reasonDate-hint").text() mustBe
        msgs("sa1.otherReason.date.hint")

      doc.select("input[id='reasonDate.day']").size() mustBe 1
      doc.select("input[id='reasonDate.month']").size() mustBe 1
      doc.select("input[id='reasonDate.year']").size() mustBe 1
    }

    "render an error summary when the form has errors" in {
      val form = formProvider()(msgs).bind(
        Map(
          "reason"           -> "",
          "reasonDate.day"   -> "",
          "reasonDate.month" -> "",
          "reasonDate.year"  -> ""
        )
      )

      val doc = Jsoup.parse(view(form, userAnswersId)(request, msgs).body)

      doc.select(".govuk-error-summary").size() mustBe 1
      doc.select(".govuk-error-summary__title").text() mustBe msgs("error.summary.title")
    }

    "render the reason and date when the form is populated" in {
      val otherReason = OtherReason(
        reason = "Test reason",
        reasonDate = LocalDate.of(2020, 5, 10)
      )

      val form = formProvider()(msgs).fill(otherReason)
      val doc  = Jsoup.parse(view(form, userAnswersId)(request, msgs).body)

      doc.select("#reason").text() mustBe "Test reason"

      doc.select("input[id='reasonDate.day']").attr("value") mustBe "10"
      doc.select("input[id='reasonDate.month']").attr("value") mustBe "5"
      doc.select("input[id='reasonDate.year']").attr("value") mustBe "2020"
    }

    "render the Continue button and correct form action" in {
      val form        = formProvider()(msgs)
      val doc         = Jsoup.parse(view(form, userAnswersId)(request, msgs).body)
      val formElement = doc.select("form")

      formElement.size() mustBe 1
      formElement.attr("action") mustBe controllers.sa1.routes.OtherReasonController
        .onSubmit(userAnswersId, models.NormalMode)
        .url
      formElement.select("button[type=submit]").text() mustBe msgs("site.continue")
    }
  }
}
