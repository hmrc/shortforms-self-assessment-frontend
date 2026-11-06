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
import org.jsoup.Jsoup
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import views.html.PageNotFoundView
import config.FrontendAppConfig
import play.api.test.FakeRequest

class PageNotFoundViewSpec extends SpecBase with GuiceOneAppPerSuite {

  private val view      = app.injector.instanceOf[PageNotFoundView]
  private val appConfig = app.injector.instanceOf[FrontendAppConfig]
  private val request   = FakeRequest()
  val msgs              = messages(app)

  "PageNotFoundView" - {
    "render the page title and main heading" in {
      val doc = Jsoup.parse(view()(request, msgs).body)

      doc.title() must include(msgs("pageNotFound.title"))
      doc.select("h1").size() mustBe 1
      doc.select("h1").text() mustBe msgs("pageNotFound.heading")
    }

    "must render the paragraphs" in {
      val doc        = Jsoup.parse(view()(request, msgs).body)
      val paragraphs = doc.select("p").eachText().toArray.toSeq

      paragraphs must contain(msgs("pageNotFound.p1"))
      paragraphs must contain(msgs("pageNotFound.p2"))
    }

    "must render the embedded links" in {
      val doc = Jsoup.parse(view()(request, msgs).body)

      val hmrcOnlineServicesHelpdeskUrlLink = doc.select(s"""a[href="${appConfig.hmrcOnlineServicesHelpdeskUrl}"]""")
      hmrcOnlineServicesHelpdeskUrlLink.size() mustBe 1
      hmrcOnlineServicesHelpdeskUrlLink.text() mustBe
        msgs("pageNotFound.p3.linkText")
      hmrcOnlineServicesHelpdeskUrlLink.attr("target") mustBe "_blank"
      hmrcOnlineServicesHelpdeskUrlLink.attr("rel") must include("noopener")
      hmrcOnlineServicesHelpdeskUrlLink.attr("rel") must include("noreferrer")
    }
  }
}
