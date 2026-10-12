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
import config.FrontendAppConfig
import org.jsoup.Jsoup
import play.api.test.FakeRequest
import play.api.test.Helpers.running
import views.html.sa1.StartView

class StartViewSpec extends SpecBase {

  "Start view" - {

    "must render the page title and main heading" in {
      val application = applicationBuilder().build()

      running(application) {
        val view = application.injector.instanceOf[StartView]
        val request = FakeRequest()
        val msgs = messages(application)

        val doc = Jsoup.parse(view()(request, msgs).body)

        doc.title() must include(msgs("sa1.start.title"))
        doc.select("span.govuk-caption-l").text() mustBe
          msgs("site.sa.caption")
        doc.select("h1").size() mustBe 1
        doc.select("h1").text() mustBe msgs("sa1.start.title")
      }
    }

    "must render the introductory content" in {
      val application = applicationBuilder().build()

      running(application) {
        val view = application.injector.instanceOf[StartView]
        val request = FakeRequest()
        val msgs = messages(application)

        val doc = Jsoup.parse(view()(request, msgs).body)

        doc.select("p.govuk-body")
          .eachText()
          .toArray
          .toSeq must contain(msgs("sa1.start.intro"))
      }
    }

    "must render the section headings" in {
      val application = applicationBuilder().build()

      running(application) {
        val view = application.injector.instanceOf[StartView]
        val request = FakeRequest()
        val msgs = messages(application)

        val doc = Jsoup.parse(view()(request, msgs).body)
        val headings = doc.select("h2").eachText().toArray.toSeq

        headings must contain(msgs("sa1.start.directors.heading"))
        headings must contain(msgs("sa1.start.childBenefitCharge.heading"))
        headings must contain(msgs("sa1.start.requirements.heading"))
      }
    }

    "must render the reasons bullet list" in {
      val application = applicationBuilder().build()

      running(application) {
        val view = application.injector.instanceOf[StartView]
        val request = FakeRequest()
        val msgs = messages(application)

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
    }

    "must render the requirements bullet list" in {
      val application = applicationBuilder().build()

      running(application) {
        val view = application.injector.instanceOf[StartView]
        val request = FakeRequest()
        val msgs = messages(application)

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
    }

    "must render the external links with appropriate attributes" in {
      val application = applicationBuilder().build()

      running(application) {
        val view = application.injector.instanceOf[StartView]
        val appConfig = application.injector.instanceOf[FrontendAppConfig]
        val request = FakeRequest()
        val msgs = messages(application)

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
    }

    "must render the Start button inside the form with the correct action" in {
      val application = applicationBuilder().build()

      running(application) {
        val view = application.injector.instanceOf[StartView]
        val request = FakeRequest()
        val msgs = messages(application)

        val doc = Jsoup.parse(view()(request, msgs).body)
        val form = doc.select("form")

        form.size() mustBe 1
        form.attr("action") mustBe
          controllers.sa1.routes.StartController.onSubmit().url

        form.select("button").text() mustBe msgs("site.start")
      }
    }
  }
}

