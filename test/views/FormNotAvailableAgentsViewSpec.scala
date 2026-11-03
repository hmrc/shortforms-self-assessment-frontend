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

package views

import base.SpecBase
import config.FrontendAppConfig
import org.jsoup.Jsoup
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.test.FakeRequest
import views.html.FormNotAvailableAgentsView

class FormNotAvailableAgentsViewSpec extends SpecBase with GuiceOneAppPerSuite {

  private val view      = app.injector.instanceOf[FormNotAvailableAgentsView]
  private val appConfig = app.injector.instanceOf[FrontendAppConfig]
  private val request   = FakeRequest()
  val msgs              = messages(app)

  "FormNotAvailableAgentsView" - {

    "render the page title and main heading" in {
      val doc = Jsoup.parse(view()(request, msgs).body)

      doc.title() must include(msgs("formNotAvailableAgents.title"))
      doc.select("h1").size() mustBe 1
      doc.select("h1").text() mustBe msgs("formNotAvailableAgents.heading")
    }

    "must render the paragraphs" in {
      val doc        = Jsoup.parse(view()(request, msgs).body)
      val paragraphs = doc.select("p").eachText().toArray.toSeq

      paragraphs must contain(msgs("formNotAvailableAgents.p1"))
      paragraphs must contain(msgs("formNotAvailableAgents.p3"))
    }

    "must render the embedded links" in {
      val doc = Jsoup.parse(view()(request, msgs).body)

      val enrolForAgentServicesAccountUrlLink =
        doc.select(s"""a[href="${appConfig.enrolForAgentServicesAccountUrl}"]""")
      enrolForAgentServicesAccountUrlLink.size() mustBe 1
      enrolForAgentServicesAccountUrlLink.text() mustBe
        msgs("formNotAvailableAgents.p2.linkText")

      val createEnrolmentUrlLink = doc.select(s"""a[href="${appConfig.createEnrolmentUrl}"]""")
      createEnrolmentUrlLink.size() mustBe 1
      createEnrolmentUrlLink.text() mustBe
        msgs("formNotAvailableAgents.p4.linkText")

      val hmrcOnlineServicesHelpdeskUrlLink = doc.select(s"""a[href="${appConfig.hmrcOnlineServicesHelpdeskUrl}"]""")
      hmrcOnlineServicesHelpdeskUrlLink.size() mustBe 1
      hmrcOnlineServicesHelpdeskUrlLink.text() mustBe
        msgs("formNotAvailableAgents.p5.linkText")
      hmrcOnlineServicesHelpdeskUrlLink.attr("target") mustBe "_blank"
      hmrcOnlineServicesHelpdeskUrlLink.attr("rel") must include("noopener")
      hmrcOnlineServicesHelpdeskUrlLink.attr("rel") must include("noreferrer")
    }

    "must render the section headings" in {
      val doc      = Jsoup.parse(view()(request, msgs).body)
      val headings = doc.select("h2").eachText().toArray.toSeq

      headings must contain(msgs("formNotAvailableAgents.subHeading1"))
      headings must contain(msgs("formNotAvailableAgents.subHeading2"))
    }

    "must render the bullet points" in {
      val doc         = Jsoup.parse(view()(request, msgs).body)
      val bulletItems = doc.select("ul.govuk-list--bullet li")

      bulletItems.get(0).text() mustEqual msgs("formNotAvailableAgents.b1")
      bulletItems.get(1).text() mustEqual msgs("formNotAvailableAgents.b2")
      bulletItems.get(2).text() mustEqual msgs("formNotAvailableAgents.b3")
      bulletItems.get(3).text() mustEqual msgs("formNotAvailableAgents.b4")

      bulletItems.size() mustEqual 4
    }
  }
}
