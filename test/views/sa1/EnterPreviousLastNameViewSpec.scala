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

import forms.sa1.PreviousNameFormProvider
import models.NormalMode
import models.sa1.PreviousName
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.test.FakeRequest
import play.api.test.Helpers.stubMessages
import views.html.sa1.EnterPreviousLastNameView

import java.time.LocalDate

class EnterPreviousLastNameViewSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite {

  private val msgs          = stubMessages()
  private val view          = app.injector.instanceOf[EnterPreviousLastNameView]
  private val formProvider  = app.injector.instanceOf[PreviousNameFormProvider]
  private val request       = FakeRequest()
  private val userAnswersId = "test-journey-id"

  "EnterPreviousLastNameView" should {

    "must display the correct content" in {
      val form = formProvider()(msgs)

      val doc = Jsoup.parse(view(form, userAnswersId)(request, msgs).toString)

      doc.title() must include(msgs("sa1.enterPreviousLastName.title"))

      doc.select("h1").text() mustBe msgs("sa1.enterPreviousLastName.heading")

      doc.select("#previousLastName").size mustBe 1

      doc.select("label[for=previousLastName]").text() must include(msgs("sa1.enterPreviousLastName.label"))

      doc.select("#lastNameChangeDate-hint").text() mustBe msgs("sa1.enterPreviousLastName.date.hint")

      doc.select("button[type=submit]").text() mustBe msgs("site.continue")

      doc.select("form").attr("action") mustBe
        controllers.sa1.routes.EnterPreviousLastNameController.onSubmit(userAnswersId, NormalMode).url
    }

    "must display an error summary when the form contains errors" in {
      val form = formProvider()(msgs).bind(
        Map(
          "previousLastName"         -> "",
          "lastNameChangeDate.day"   -> "",
          "lastNameChangeDate.month" -> "",
          "lastNameChangeDate.year"  -> ""
        )
      )

      val doc = Jsoup.parse(view(form, userAnswersId)(request, msgs).toString)

      doc.select(".govuk-error-summary").size mustBe 1
      doc.select(".govuk-error-summary__title").text() mustBe msgs("error.summary.title")
    }

    "must display the previous last name when the form is populated" in {
      val form = formProvider()(msgs).fill(
        PreviousName(
          previousLastName = "John Doe",
          lastNameChangeDate = LocalDate.of(2020, 5, 10)
        )
      )

      val doc = Jsoup.parse(view(form, userAnswersId)(request, msgs).toString)

      doc.select("#previousLastName").attr("value") mustBe "John Doe"
      doc.select("input[id='lastNameChangeDate.day']").attr("value") mustBe "10"
      doc.select("input[id='lastNameChangeDate.month']").attr("value") mustBe "5"
      doc.select("input[id='lastNameChangeDate.year']").attr("value") mustBe "2020"
    }
  }
}
