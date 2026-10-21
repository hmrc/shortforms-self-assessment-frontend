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
import forms.sa1.DoYouHaveANinoFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import play.api.test.FakeRequest
import play.api.test.Helpers.running
import views.html.sa1.DoYouHaveANinoView

import scala.jdk.CollectionConverters.*

class DoYouHaveANinoViewSpec extends SpecBase {

  private val doYouHaveANinoFormProvider = new DoYouHaveANinoFormProvider()
  protected val form                     = doYouHaveANinoFormProvider()

  "DoYouHaveANinoView" - {

    "must render the page title and main heading" in {

      val application = applicationBuilder().build()

      running(application) {

        val view    = application.injector.instanceOf[DoYouHaveANinoView]
        val request = FakeRequest()
        val msgs    = messages(application)

        val doc =
          Jsoup.parse(
            view(form, userAnswersId, NormalMode)(request, msgs).body
          )

        doc.title() must include(msgs("sa1.doYouHaveANino.title"))

        doc.select("h1").size() mustEqual 1
        doc.select("h1").text() mustEqual msgs("sa1.doYouHaveANino.heading")
      }
    }

    "must render the page content" in {

      val application = applicationBuilder().build()

      running(application) {

        val view    = application.injector.instanceOf[DoYouHaveANinoView]
        val request = FakeRequest()
        val msgs    = messages(application)

        val doc =
          Jsoup.parse(
            view(form, userAnswersId, NormalMode)(request, msgs).body
          )

        doc.select("p").text() must include(msgs("sa1.doYouHaveANino.p1"))

        val bulletItems =
          doc.select("ul.govuk-list--bullet li")

        bulletItems.size() mustEqual 4

        bulletItems.get(0).text() mustEqual msgs("sa1.doYouHaveANino.b1")
        bulletItems.get(1).text() mustEqual msgs("sa1.doYouHaveANino.b2")
        bulletItems.get(2).text() mustEqual msgs("sa1.doYouHaveANino.b3")
        bulletItems.get(3).text() mustEqual msgs("sa1.doYouHaveANino.b4")
      }
    }

    "must render the paragraph and link to find a National Insurance number" in {

      val application = applicationBuilder().build()

      running(application) {

        val view    = application.injector.instanceOf[DoYouHaveANinoView]
        val request = FakeRequest()
        val msgs    = messages(application)

        val doc = Jsoup.parse(
          view(form, userAnswersId, NormalMode)(request, msgs).body
        )

        doc.body.text() must include(
          "If none of these apply to you, you can still register for Self Assessment without one."
        )

        val link = doc
          .select("a")
          .asScala
          .find(_.text() == msgs("sa1.doYouHaveANino.p2.linkText"))
          .value

        link.text() mustEqual msgs("sa1.doYouHaveANino.p2.linkText")
        link.attr("target") mustBe "_blank"
        link.attr("rel") must include("noopener")
        link.attr("rel") must include("noreferrer")
      }
    }

    "must render yes and no radio buttons" in {

      val application = applicationBuilder().build()

      running(application) {

        val view    = application.injector.instanceOf[DoYouHaveANinoView]
        val request = FakeRequest()
        val msgs    = messages(application)

        val doc =
          Jsoup.parse(
            view(form, userAnswersId, NormalMode)(request, msgs).body
          )

        doc.select("input[type=radio]").size() mustEqual 2

        doc.getElementById("value").attr("value") mustEqual "true"
        doc.getElementById("value-2").attr("value") mustEqual "false"
      }
    }

    "must render the continue button" in {

      val application = applicationBuilder().build()

      running(application) {

        val view    = application.injector.instanceOf[DoYouHaveANinoView]
        val request = FakeRequest()
        val msgs    = messages(application)

        val doc =
          Jsoup.parse(
            view(form, userAnswersId, NormalMode)(request, msgs).body
          )

        doc.select("button").text() mustEqual msgs("site.continue")
      }
    }

    "must render an error summary when the form contains errors" in {

      val application = applicationBuilder().build()

      running(application) {

        val view    = application.injector.instanceOf[DoYouHaveANinoView]
        val request = FakeRequest()
        val msgs    = messages(application)

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
}
