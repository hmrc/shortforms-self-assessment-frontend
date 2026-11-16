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

import config.FrontendAppConfig
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.test.FakeRequest
import play.api.test.Helpers.stubMessages
import views.html.cwf1.StartView

class StartViewSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite {

  private val view      = app.injector.instanceOf[StartView]
  private val appConfig = app.injector.instanceOf[FrontendAppConfig]
  private val request   = FakeRequest()
  private val msgs      = stubMessages()

  "Start view" should {

    "render the page title and main heading" in {
      val doc = Jsoup.parse(view()(request, msgs).body)

      doc.title() must include(msgs("cwf1.start.title"))
      doc.select("span.govuk-caption-l").text() mustBe
        msgs("site.sa.caption")
      doc.select("h1").size() mustBe 1
      doc.select("h1").text() mustBe msgs("cwf1.start.title")
    }

    "render the introductory content" in {
      val doc = Jsoup.parse(view()(request, msgs).body)

      doc
        .select("p.govuk-body")
        .eachText()
        .toArray
        .toSeq must contain(msgs("cwf1.start.intro"))
    }

    "render the section headings" in {
      val doc      = Jsoup.parse(view()(request, msgs).body)
      val headings = doc.select("h2").eachText().toArray.toSeq

      headings must contain(msgs("cwf1.start.agents.heading"))
      headings must contain(msgs("cwf1.start.directors.heading"))
      headings must contain(msgs("cwf1.start.partners.heading"))
      headings must contain(msgs("cwf1.start.tradingAllowance.heading"))
      headings must contain(msgs("cwf1.start.requirements.heading"))
    }

    "render the agents and directors content" in {
      val doc = Jsoup.parse(view()(request, msgs).body)

      val paragraphs = doc.select("p.govuk-body").eachText().toArray.toSeq

      paragraphs must contain(msgs("cwf1.start.agents.content"))
    }

    "render the directors link to the SA1 start page" in {
      val doc = Jsoup.parse(view()(request, msgs).body)

      val directorsLink = doc.select(
        s"""a[href="${controllers.sa1.routes.StartController.onPageLoad().url}"]"""
      )

      directorsLink.size() mustBe 1
      directorsLink.text() mustBe msgs("cwf1.start.directors.linkText")
    }

    "render the partners content and form links" in {
      val doc = Jsoup.parse(view()(request, msgs).body)

      val paragraphs = doc.select("p.govuk-body").eachText().toArray.toSeq

      paragraphs must contain(msgs("cwf1.start.partners.content"))

      val sa400Link = doc.select(s"""a[href="${appConfig.formSA400Url}"]""")
      sa400Link.size() mustBe 1
      sa400Link.text() mustBe msgs("cwf1.start.partners.sa400.linkText")

      val sa401Link = doc.select(s"""a[href="${appConfig.formSA401Url}"]""")
      sa401Link.size() mustBe 1
      sa401Link.text() mustBe msgs("cwf1.start.partners.sa401.linkText")

      val sa402Link = doc.select(s"""a[href="${appConfig.formSA402Url}"]""")
      sa402Link.size() mustBe 1
      sa402Link.text() mustBe msgs("cwf1.start.partners.sa402.linkText")
    }

    "render the trading allowance guidance link with appropriate attributes" in {
      val doc = Jsoup.parse(view()(request, msgs).body)

      val tradingAllowanceLink = doc.select(
        s"""a[href="${appConfig.guidanceOnPropertyTradingUrl}"]"""
      )

      tradingAllowanceLink.size() mustBe 1
      tradingAllowanceLink.text() mustBe
        msgs("cwf1.start.tradingAllowance.linkText")
      tradingAllowanceLink.attr("target") mustBe "_blank"
      tradingAllowanceLink.attr("rel") must include("noopener")
      tradingAllowanceLink.attr("rel") must include("noreferrer")
    }

    "render the requirements bullet list" in {
      val doc = Jsoup.parse(view()(request, msgs).body)

      val requirementKeys = Seq(
        "cwf1.start.requirements.nationalInsuranceNumber",
        "cwf1.start.requirements.utr"
      )

      val listItems = doc.select("li").eachText().toArray.toSeq

      requirementKeys.foreach { key =>
        listItems must contain(msgs(key))
      }
    }

    "render the save form content" in {
      val doc = Jsoup.parse(view()(request, msgs).body)

      doc
        .select("p.govuk-body")
        .eachText()
        .toArray
        .toSeq must contain(msgs("cwf1.start.save.form"))
    }

    "render the Start button inside the form with the correct action" in {
      val doc  = Jsoup.parse(view()(request, msgs).body)
      val form = doc.select("form")

      form.size() mustBe 1
      form.attr("action") mustBe
        controllers.cwf1.routes.StartController.onSubmit().url

      form.select("button").text() mustBe msgs("site.start")
    }
  }
}
