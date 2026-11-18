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
import play.api.test.FakeRequest
import views.html.ResubmissionErrorView

class ResubmissionErrorViewSpec extends SpecBase with GuiceOneAppPerSuite {

  private val view    = app.injector.instanceOf[ResubmissionErrorView]
  private val request = FakeRequest()
  val msgs            = messages(app)

  "ResubmissionErrorView" - {
    "render the page title and main heading" in {
      val doc = Jsoup.parse(view()(request, msgs).body)

      doc.title() must include(msgs("resubmissionError.title"))
      doc.select("h1").size() mustBe 1
      doc.select("h1").text() mustBe msgs("resubmissionError.heading")
    }

    "must render the paragraphs" in {
      val doc        = Jsoup.parse(view()(request, msgs).body)
      val paragraphs = doc.select("p").eachText().toArray.toSeq

      paragraphs must contain(msgs("resubmissionError.p1"))

      paragraphs must contain(
        s"You can ${msgs("resubmissionError.p2.linkText1")} or ${msgs("resubmissionError.p2.linkText2")}."
      )
    }

    "must render the embedded links" in {
      val doc = Jsoup.parse(view()(request, msgs).body)

      val link1 = doc.select(
        """a[href="/self-assessment-forms/sa1/start"]"""
      )
      link1.size() mustBe 1
      link1.text() mustBe msgs("resubmissionError.p2.linkText1")

      val link2 = doc.select(
        """a[href="/home"]"""
      )
      link2.size() mustBe 1
      link2.text() mustBe msgs("resubmissionError.p2.linkText2")
    }
  }
}
