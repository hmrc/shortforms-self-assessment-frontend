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

package handlers

import base.SpecBase
import org.jsoup.Jsoup
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.test.FakeRequest
import play.api.test.Helpers.*

class ErrorHandlerSpec extends SpecBase with GuiceOneAppPerSuite {

  private val errorHandler = app.injector.instanceOf[ErrorHandler]

  "ErrorHandler" - {
    "must render the start again page for an unhandled server error" in {
      val msgs   = messages(app)
      val result = errorHandler.onServerError(FakeRequest(), new RuntimeException("boom"))

      status(result) mustBe INTERNAL_SERVER_ERROR
      val doc = Jsoup.parse(contentAsString(result))
      doc.select("h1").text() mustBe msgs("journeyRecovery.startAgain.heading")
      doc.select(s"""a.govuk-button[href="${controllers.routes.IndexController.onPageLoad().url}"]""").text() mustBe
        msgs("site.startAgain")
    }
  }
}
