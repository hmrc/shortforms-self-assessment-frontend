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

import forms.sa1.MoveAddressFormProvider
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.test.FakeRequest
import play.api.test.Helpers.stubMessages
import views.html.sa1.MoveAddressView

import java.time.LocalDate

class MoveAddressViewSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite {

  private val journeyId    = "test-journey-id"
  private val msgs         = stubMessages()
  private val view         = app.injector.instanceOf[MoveAddressView]
  private val formProvider = app.injector.instanceOf[MoveAddressFormProvider]
  private val request      = FakeRequest()

  "MoveAddressView" should {

    "render the page title and heading" in {
      val form = formProvider()(msgs)
      val doc  = Jsoup.parse(view(form, journeyId)(request, msgs).body)

      doc.title() must include(msgs("sa1.moveAddress.title"))
      doc.select("h1").text() mustBe msgs("sa1.moveAddress.heading")
    }

    "render the date hint and input fields" in {
      val form = formProvider()(msgs)
      val doc  = Jsoup.parse(view(form, journeyId)(request, msgs).body)

      doc.select("#moveAddressDate-hint").text() mustBe
        msgs("sa1.moveAddress.hint")

      doc.select("input[id='moveAddressDate.day']").size() mustBe 1
      doc.select("input[id='moveAddressDate.month']").size() mustBe 1
      doc.select("input[id='moveAddressDate.year']").size() mustBe 1
    }

    "render an error summary when the form has errors" in {
      val form = formProvider()(msgs).bind(
        Map(
          "moveAddressDate.day"   -> "",
          "moveAddressDate.month" -> "",
          "moveAddressDate.year"  -> ""
        )
      )

      val doc = Jsoup.parse(view(form, journeyId)(request, msgs).body)

      doc.select(".govuk-error-summary").size() mustBe 1
      doc.select(".govuk-error-summary__title").text() mustBe
        msgs("error.summary.title")
    }

    "render the date when the form is populated" in {
      val date = LocalDate.of(1990, 5, 10)
      val form = formProvider()(msgs).fill(date)

      val doc = Jsoup.parse(view(form, journeyId)(request, msgs).body)

      doc.select("input[id='moveAddressDate.day']").attr("value") mustBe "10"
      doc.select("input[id='moveAddressDate.month']").attr("value") mustBe "5"
      doc.select("input[id='moveAddressDate.year']").attr("value") mustBe "1990"
    }

    "render the Continue button and correct form action" in {
      val form        = formProvider()(msgs)
      val doc         = Jsoup.parse(view(form, journeyId)(request, msgs).body)
      val formElement = doc.select("form")

      formElement.size() mustBe 1
      formElement
        .attr("action") mustBe controllers.sa1.routes.MoveAddressController.onSubmit(journeyId, models.NormalMode).url

      formElement.select("button[type=submit]").text() mustBe msgs("site.continue")
    }
  }
}
