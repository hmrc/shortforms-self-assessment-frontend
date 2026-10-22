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
import forms.sa1.AddPreviousLastNameFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.test.FakeRequest
import play.api.test.Helpers.running
import views.html.sa1.AddPreviousLastNameView

class AddPreviousLastNameViewSpec extends SpecBase with GuiceOneAppPerSuite {

  private val addPreviousLastNameFormProvider = new AddPreviousLastNameFormProvider()
  protected val form                          = addPreviousLastNameFormProvider()

  "AddPreviousLastNameView" - {

    "must render the page title and main heading" in {

      val view    = app.injector.instanceOf[AddPreviousLastNameView]
      val request = FakeRequest()
      val msgs    = messages(app)

      val doc =
        Jsoup.parse(
          view(form, userAnswersId, NormalMode)(request, msgs).body
        )

      doc.title() must include(msgs("sa1.addPreviousLastName.title"))

      doc.select("h1").size() mustEqual 1
      doc.select("h1").text() mustEqual msgs("sa1.addPreviousLastName.heading")
    }

    "must render yes and no radio buttons" in {

      val view    = app.injector.instanceOf[AddPreviousLastNameView]
      val request = FakeRequest()
      val msgs    = messages(app)

      val doc =
        Jsoup.parse(
          view(form, userAnswersId, NormalMode)(request, msgs).body
        )

      doc.select("input[type=radio]").size() mustEqual 2

      doc.getElementById("value").attr("value") mustEqual "true"
      doc.getElementById("value-2").attr("value") mustEqual "false"
    }

    "must render the continue button" in {

      val view    = app.injector.instanceOf[AddPreviousLastNameView]
      val request = FakeRequest()
      val msgs    = messages(app)

      val doc =
        Jsoup.parse(
          view(form, userAnswersId, NormalMode)(request, msgs).body
        )

      doc.select("button").text() mustEqual msgs("site.continue")
    }

    "must render an error summary when the form contains errors" in {

      val view    = app.injector.instanceOf[AddPreviousLastNameView]
      val request = FakeRequest()
      val msgs    = messages(app)

      val formWithErrors =
        form.bind(Map("value" -> ""))

      val doc =
        Jsoup.parse(
          view(formWithErrors, userAnswersId, NormalMode)(request, msgs).body
        )

      doc.select(".govuk-error-summary").size() mustEqual 1
    }
  }
}
