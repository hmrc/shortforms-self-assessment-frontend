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
import forms.cwf1.AgentDetailsFormProvider
import models.NormalMode
import models.cwf1.AgentDetails
import org.jsoup.Jsoup
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.test.FakeRequest
import play.api.test.Helpers.stubMessages
import views.html.cwf1.AgentDetailsView

class AgentDetailsViewSpec extends SpecBase with GuiceOneAppPerSuite {

  private val msgs         = stubMessages()
  private val view         = app.injector.instanceOf[AgentDetailsView]
  private val formProvider = new AgentDetailsFormProvider()
  private val form         = formProvider()
  private val journeyId    = "test-journey-id"

  "AgentDetailsView" - {

    "must render the title, heading" in {
      val doc = Jsoup.parse(view(form, journeyId, NormalMode)(FakeRequest(), msgs).body)

      doc.title() must include(msgs("cwf1.agentDetails.title"))
      doc.select("h1").text() mustBe msgs("cwf1.agentDetails.heading")
    }

    "must render agent details client reference and full name inputs" in {
      val doc = Jsoup.parse(view(form, journeyId, NormalMode)(FakeRequest(), msgs).body)

      doc.select("input#fullName").size() mustBe 1
      doc.select("input#clientReference").size() mustBe 1
      doc.select("label[for=fullName]").text() mustBe msgs("cwf1.agentDetails.fullName")
      doc.select("label[for=clientReference]").text() mustBe msgs("cwf1.agentDetails.clientReference")
    }

    "must pre-fill the inputs from the supplied form" in {
      val filledForm = form.fill(AgentDetails("Mary Jane", Some("12345678")))

      val doc = Jsoup.parse(view(filledForm, journeyId, NormalMode)(FakeRequest(), msgs).body)

      doc.select("input#fullName").attr("value") mustBe "Mary Jane"
      doc.select("input#clientReference").attr("value") mustBe "12345678"
    }

    "must show an error summary and error messages when the form has errors" in {
      val invalidForm = form.bind(Map("clientReference" -> "", "fullName" -> ""))

      val doc = Jsoup.parse(view(invalidForm, journeyId, NormalMode)(FakeRequest(), msgs).body)

      doc.select(".govuk-error-summary").size() mustBe 1
      doc.select(".govuk-error-message").text() must include(msgs("cwf1.agentDetails.error.fullName.required"))
    }
  }
}
