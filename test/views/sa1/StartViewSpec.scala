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

import config.FrontendAppConfig
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.test.FakeRequest
import play.api.test.Helpers.stubMessages
import views.html.sa1.StartView

class StartViewSpec
  extends AnyWordSpec
    with Matchers
    with GuiceOneAppPerSuite {

  private val view = app.injector.instanceOf[StartView]
  private val appConfig = app.injector.instanceOf[FrontendAppConfig]
  private val request = FakeRequest()
  private val msgs = stubMessages()

  "Start view" should {

    "render the page title and main heading" in {
      val doc = Jsoup.parse(view()(request, msgs).body)

      doc.title() must include(msgs("sa1.start.title"))
      doc.select("span.govuk-caption-l").text() mustBe
        msgs("site.registerForSa.caption")
      doc.select("h1").size() mustBe 1
      doc.select("h1").text() mustBe msgs("sa1.start.title")
    }

    "render the introductory content" in {
      val doc = Jsoup.parse(view()(request, msgs).body)

      doc.select("p.govuk-body")
        .eachText()
        .toArray
        .toSeq must contain(msgs("sa1.start.intro"))
    }

    "render the section headings" in {
      val doc = Jsoup.parse(view()(request, msgs).body)
      val headings = doc.select("h2").eachText().toArray.toSeq

      headings must contain(msgs("sa1.start.directors.heading"))
      headings must contain(msgs("sa1.start.childBenefitCharge.heading"))
      headings must contain(msgs("sa1.start.requirements.heading"))
    }

    "render the reasons bullet list" in {
      val doc = Jsoup.parse(view()(request, msgs).body)

      val reasonKeys = Seq(
        "sa1.start.reasons.director",
        "sa1.start.reasons.childBenefitCharge",
        "sa1.start.reasons.ukPropertyIncome",
        "sa1.start.reasons.foreignIncome",
        "sa1.start.reasons.trustIncome",
        "sa1.start.reasons.incomeThresholds",
        "sa1.start.reasons.untaxedIncome",
        "sa1.start.reasons.capitalGainsTax"
      )

      val listItems = doc.select("li").eachText().toArray.toSeq

      reasonKeys.foreach { key =>
        listItems must contain(msgs(key))
      }
    }

    "render the requirements bullet list" in {
      val doc = Jsoup.parse(view()(request, msgs).body)

      val requirementKeys = Seq(
        "sa1.start.requirements.nationalInsuranceNumber",
        "sa1.start.requirements.incomeOrBenefitDates"
      )

      val listItems = doc.select("li").eachText().toArray.toSeq

      requirementKeys.foreach { key =>
        listItems must contain(msgs(key))
      }
    }

    "render the external links with appropriate attributes" in {
      val doc = Jsoup.parse(view()(request, msgs).body)

        val payeLink = doc.select(
          s"""a[href="${appConfig.highIncomeChildBenefitChargeUrl}"]"""
        )

        payeLink.size() mustBe 1
        payeLink.text() mustBe
          msgs("sa1.start.childBenefitCharge.payeService.linkText")
        payeLink.attr("target") mustBe "_blank"
        payeLink.attr("rel") must include("noopener")
        payeLink.attr("rel") must include("noreferrer")

        val ninoLink = doc.select(
          s"""a[href="${appConfig.applyNinoUrl}"]"""
        )

      ninoLink.size() mustBe 1
      ninoLink.text() mustBe
        msgs("sa1.start.requirements.applyForNationalInsuranceNumber.linkText")
      ninoLink.attr("target") mustBe "_blank"
      ninoLink.attr("rel") must include("noopener")
      ninoLink.attr("rel") must include("noreferrer")
    }

    "render the Start button inside the form with the correct action" in {
      val doc = Jsoup.parse(view()(request, msgs).body)
      val form = doc.select("form")

        form.size() mustBe 1
        form.attr("action") mustBe
          controllers.sa1.routes.StartController.onSubmit().url

      form.select("button").text() mustBe msgs("site.start")
    }
  }
}

