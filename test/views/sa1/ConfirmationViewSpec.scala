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
import org.jsoup.Jsoup
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.test.FakeRequest
import views.html.sa1.ConfirmationView

class ConfirmationViewSpec extends SpecBase with GuiceOneAppPerSuite {

  private lazy val msgs = messages(app)
  private lazy val view = app.injector.instanceOf[ConfirmationView]

  "ConfirmationView" - {

    "render the confirmation panel, submission time and next steps" in {
      val doc = Jsoup.parse(view("15:45 on 16 October 2026", "AB123CD")(FakeRequest(), msgs).body)

      doc.title() mustBe s"Confirmation - ${msgs("service.name")} - ${msgs("site.govuk")}"
      doc.select("a.govuk-back-link").size() mustBe 0
      doc.select(".govuk-panel--confirmation .govuk-panel__title").text() mustBe
        msgs("sa1.confirmation.panel.heading")
      doc.select(".govuk-panel--confirmation .govuk-panel__body").text() mustBe
        msgs("sa1.confirmation.panel.reference", "AB123CD")
      doc.select(".govuk-panel--confirmation .govuk-panel__body br").size() mustBe 1
      doc.select(".govuk-panel--confirmation .govuk-panel__body strong").text() mustBe "AB123CD"
      doc.select("p.govuk-body").get(0).text() mustBe
        msgs("sa1.confirmation.submittedAt", "15:45 on 16 October 2026")
      doc.select("h2.govuk-heading-m").text() mustBe msgs("sa1.confirmation.next.heading")
      doc.select("p.govuk-body").get(1).text() mustBe msgs("sa1.confirmation.next.content")
    }

    "render the account link to the current host's home page with screen-reader context" in {
      val doc  = Jsoup.parse(view("15:45 on 16 October 2026", "AB123CD")(FakeRequest(), msgs).body)
      val link = doc.select("a.govuk-link[href='/home']")

      link.size() mustBe 1
      link.attr("href") mustBe "/home"
      link.text() mustBe s"${msgs("sa1.confirmation.backToAccount")} ${msgs("sa1.confirmation.backToAccount.hidden")}"
    }
  }
}
