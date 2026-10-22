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

import forms.sa1.DateLandPropertyFormProvider
import models.{NormalMode, CheckMode}
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.test.FakeRequest
import play.api.test.Helpers.stubMessages
import views.html.sa1.DateLandPropertyView

import java.time.LocalDate

class DateLandPropertyViewSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite {

  private val msgs          = stubMessages()
  private val view          = app.injector.instanceOf[DateLandPropertyView]
  private val formProvider  = new DateLandPropertyFormProvider()
  private val request       = FakeRequest()
  private val userAnswersId = "test-journey-id"

  "DateLandPropertyView" should {

    "must display the correct content" in {
      val form = formProvider()(msgs)

      val doc = Jsoup.parse(view(form, userAnswersId, NormalMode)(request, msgs).toString)

      doc.title() must include(msgs("sa1.dateLandPropertyUk.title"))

      doc.select("h1").text() mustBe msgs("sa1.dateLandPropertyUk.heading")

      doc.select("#dateLandPropertyUk-hint").text() mustBe msgs("sa1.dateLandPropertyUk.hint")

      doc.select("input[id='dateLandPropertyUk.day']").size mustBe 1
      doc.select("input[id='dateLandPropertyUk.month']").size mustBe 1
      doc.select("input[id='dateLandPropertyUk.year']").size mustBe 1

      doc.select("button[type=submit]").text() mustBe msgs("site.continue")

      doc.select("form").attr("action") mustBe
        controllers.sa1.routes.DateLandPropertyController.onSubmit(userAnswersId, NormalMode).url
    }

    "must post to the change route in check mode" in {
      val form = formProvider()(msgs)

      val doc = Jsoup.parse(view(form, userAnswersId, CheckMode)(request, msgs).toString)

      doc.select("form").attr("action") mustBe
        controllers.sa1.routes.DateLandPropertyController.onSubmit(userAnswersId, CheckMode).url
    }

    "must display an error summary when the form contains errors" in {
      val form = formProvider()(msgs).bind(
        Map(
          "dateLandPropertyUk.day"   -> "",
          "dateLandPropertyUk.month" -> "",
          "dateLandPropertyUk.year"  -> ""
        )
      )

      val doc = Jsoup.parse(view(form, userAnswersId, NormalMode)(request, msgs).toString)

      doc.select(".govuk-error-summary").size mustBe 1
      doc.select(".govuk-error-summary__title").text() mustBe msgs("error.summary.title")
      doc.select(".govuk-error-summary__list").text() must include(
        msgs("sa1.dateLandPropertyUk.date.error.required")
      )
    }

    "must display the date when the form is populated" in {
      val form = formProvider()(msgs).fill(LocalDate.of(2022, 3, 19))

      val doc = Jsoup.parse(view(form, userAnswersId, NormalMode)(request, msgs).toString)

      doc.select("input[id='dateLandPropertyUk.day']").attr("value") mustBe "19"
      doc.select("input[id='dateLandPropertyUk.month']").attr("value") mustBe "3"
      doc.select("input[id='dateLandPropertyUk.year']").attr("value") mustBe "2022"
    }
  }
}
