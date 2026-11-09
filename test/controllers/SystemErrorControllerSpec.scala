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

package controllers

import base.SpecBase
import org.jsoup.Jsoup
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.test.FakeRequest
import play.api.test.Helpers.*

class SystemErrorControllerSpec extends SpecBase with GuiceOneAppPerSuite {

  "GET /system-error" - {
    "show the error page without a reference" in {
      val result = route(app, FakeRequest(GET, routes.SystemErrorController.onPageLoad().url)).value

      status(result) mustBe OK
      val doc = Jsoup.parse(contentAsString(result))
      doc.title() must include("Sorry, there is a problem with the service")
      doc.select("h1").text() mustBe "Sorry, there is a problem with the service"
      doc.select("main").text() must include("Try again later or contact the")
      doc.select("main").text() must not include "quoting reference:"
      val link = doc.select("main a[href='https://www.gov.uk/find-hmrc-contacts/technical-support-with-hmrc-online-services']")
      link.size() mustBe 1
      link.text() mustBe "HMRC Online Services Helpdesk (opens in a new tab)"
      link.attr("target") mustBe "_blank"
      link.attr("rel") must include("noopener")
      link.attr("rel") must include("noreferrer")
    }

    "show an available reference as text" in {
      val request = FakeRequest(GET, routes.SystemErrorController.onPageLoad().url)
        .withFlash("submissionReference" -> "<ref-123>")
      val result  = route(app, request).value
      val doc     = Jsoup.parse(contentAsString(result))

      status(result) mustBe OK
      doc.select("main").text() must include("quoting reference: <ref-123>")
      doc.select("main ref-123").size() mustBe 0
    }

    "omit an empty reference" in {
      val request = FakeRequest(GET, routes.SystemErrorController.onPageLoad().url)
        .withFlash("submissionReference" -> " ")
      val result  = route(app, request).value

      status(result) mustBe OK
      contentAsString(result) must not include "quoting reference:"
    }
  }
}
