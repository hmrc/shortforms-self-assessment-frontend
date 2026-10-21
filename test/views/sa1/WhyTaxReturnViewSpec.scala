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
import forms.sa1.WhyTaxReturnFormProvider
import models.sa1.WhyTaxReturn
import models.{CheckMode, NormalMode}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.data.Form
import play.api.i18n.Messages
import play.api.test.FakeRequest
import play.api.test.Helpers.running
import views.html.sa1.WhyTaxReturnView

class WhyTaxReturnViewSpec extends SpecBase {

  private val form = new WhyTaxReturnFormProvider()()

  "WhyTaxReturnView" - {

    "must render the page title, heading and hint" in new Setup {
      private val doc = render(form)

      doc.title() must include(msgs("sa1.whyTaxReturn.title"))

      doc.select("h1").size() mustEqual 1
      doc.select("h1").text() mustEqual msgs("sa1.whyTaxReturn.heading")

      doc.select(".govuk-hint").text() mustEqual msgs("sa1.whyTaxReturn.hint")
    }

    "must render a checkbox for each reason in order" in new Setup {
      private val doc = render(form)

      private val checkboxes = doc.select("input[type=checkbox]")
      private val labels     = doc.select(".govuk-checkboxes__label")

      checkboxes.size() mustEqual WhyTaxReturn.values.size

      WhyTaxReturn.values.zipWithIndex.foreach { case (reason, index) =>
        val checkbox = checkboxes.get(index)

        checkbox.attr("name") mustEqual s"value[$index]"
        checkbox.attr("value") mustEqual reason.toString
        checkbox.attr("id") mustEqual s"value_$index"
        checkbox.hasAttr("checked") mustBe false

        labels.get(index).text() mustEqual msgs(s"sa1.whyTaxReturn.$reason")
      }
    }

    "must pre-select previously chosen reasons" in new Setup {
      private val doc = render(form.fill(Set(WhyTaxReturn.CompanyDirector, WhyTaxReturn.Other)))

      doc.select("input[type=checkbox][checked]").size() mustEqual 2
      doc.select(s"input[value=${WhyTaxReturn.CompanyDirector}]").hasAttr("checked") mustBe true
      doc.select(s"input[value=${WhyTaxReturn.Other}]").hasAttr("checked") mustBe true
    }

    "must submit to the onSubmit route for the given journey and mode" in new Setup {
      render(form).select("form").attr("action") mustEqual
        controllers.sa1.routes.WhyTaxReturnController.onSubmit(userAnswersId, NormalMode).url

      render(form, CheckMode).select("form").attr("action") mustEqual
        controllers.sa1.routes.WhyTaxReturnController.onSubmit(userAnswersId, CheckMode).url
    }

    "must render a continue button" in new Setup {
      render(form).select("button[type=submit]").text() mustEqual msgs("site.continue")
    }

    "must render the error summary and field error when nothing is selected" in new Setup {
      private val doc = render(form.bind(Map.empty[String, String]))

      doc.title() must startWith(msgs("error.title.prefix"))

      private val errorSummary = doc.select(".govuk-error-summary")
      errorSummary.size() mustEqual 1
      errorSummary.select("h2").text() mustEqual msgs("error.summary.title")

      private val errorLink = errorSummary.select("a")
      errorLink.text() mustEqual msgs("sa1.whyTaxReturn.error.required")
      errorLink.attr("href") mustEqual "#value_0"

      doc.select(".govuk-error-message").text() must include(msgs("sa1.whyTaxReturn.error.required"))
    }
  }

  private class Setup {
    private val application = applicationBuilder().build()

    protected implicit val msgs: Messages = messages(application)

    protected def render(form: Form[_], mode: models.Mode = NormalMode): Document =
      running(application) {
        val view = application.injector.instanceOf[WhyTaxReturnView]
        Jsoup.parse(view(form, userAnswersId, mode)(FakeRequest(), msgs).body)
      }
  }
}
