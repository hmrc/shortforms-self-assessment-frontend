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

import forms.cwf1.EnterUTRFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.test.FakeRequest
import play.api.test.Helpers.stubMessages
import views.html.cwf1.EnterUTRView

class EnterUTRViewSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite {

  private val msgs          = stubMessages()
  private val view          = app.injector.instanceOf[EnterUTRView]
  private val formProvider  = app.injector.instanceOf[EnterUTRFormProvider]
  private val request       = FakeRequest()
  private val userAnswersId = "test-journey-id"

  "EnterUTRView" should {

    "must display the correct content" in {
      val form = formProvider()

      val doc = Jsoup.parse(view(form, userAnswersId, NormalMode)(request, msgs).toString)

      doc.title() must include(msgs("cwf1.enterUTR.title"))

      doc.select("h1").text() mustBe msgs("cwf1.enterUTR.heading")

      doc.select("#value").size mustBe 1

      doc.select("label[for=value]").text() must include(msgs("cwf1.enterUTR.subHeading"))

      doc.select("#value-hint").text() mustBe msgs("cwf1.enterUTR.hint")

      doc.select("a.govuk-link").eq(2).text() mustBe msgs("cwf1.enterUTR.linkText")

      doc.select("button[type=submit]").text() mustBe msgs("site.continue")

      doc.select("form").attr("action") mustBe
        controllers.cwf1.routes.EnterUTRController.onSubmit(userAnswersId, NormalMode).url
    }

    "must display an error summary when the form contains errors" in {
      val form = formProvider().bind(
        Map("value" -> "")
      )

      val doc = Jsoup.parse(view(form, userAnswersId, NormalMode)(request, msgs).toString)

      doc.select(".govuk-error-summary").size mustBe 1
      doc.select(".govuk-error-summary__title").text() mustBe msgs("error.summary.title")
    }

    "must display the UTR when the form is populated" in {
      val utr = "1234567890"

      val form = formProvider().fill(utr)

      val doc = Jsoup.parse(view(form, userAnswersId, NormalMode)(request, msgs).toString)

      doc.select("#value").attr("value") mustBe utr
    }
  }
}
