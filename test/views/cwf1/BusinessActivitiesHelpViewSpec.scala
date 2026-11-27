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

import base.SpecBase
import config.FrontendAppConfig
import org.jsoup.Jsoup
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.test.FakeRequest
import views.html.cwf1.BusinessActivitiesHelpView

class BusinessActivitiesHelpViewSpec extends SpecBase with GuiceOneAppPerSuite {

  private val view      = app.injector.instanceOf[BusinessActivitiesHelpView]
  private val appConfig = app.injector.instanceOf[FrontendAppConfig]
  private val request   = FakeRequest()
  val msgs              = messages(app)

  "BusinessActivitiesHelpView" - {

    "render the page title and main heading" in {
      val doc = Jsoup.parse(view()(request, msgs).body)

      doc.title() must include(msgs("cwf1.businessActivitiesHelp.title"))

      doc.select("h1").size() mustBe 1
      doc.select("h1").text() mustBe
        msgs("cwf1.businessActivitiesHelp.heading")
    }

    "render the contents heading and links" in {
      val doc = Jsoup.parse(view()(request, msgs).body)

      doc.select("h2").eachText().toArray.toSeq must contain(
        msgs("cwf1.businessActivitiesHelp.contents")
      )

      val contentsLinks = Seq(
        ("cis", "cwf1.businessActivitiesHelp.contents.cis.linkText"),
        ("share-fishermen", "cwf1.businessActivitiesHelp.contents.shareFishermen.linkText"),
        ("land-and-property", "cwf1.businessActivitiesHelp.contents.landAndProperty.linkText"),
        ("lloyds-underwriter", "cwf1.businessActivitiesHelp.contents.lloydsUnderwriter.linkText"),
        ("directors", "cwf1.businessActivitiesHelp.contents.directors.linkText"),
        ("examiners", "cwf1.businessActivitiesHelp.contents.examiners.linkText"),
        ("minister-of-religion", "cwf1.businessActivitiesHelp.contents.ministerOfReligion.linkText"),
        ("investment", "cwf1.businessActivitiesHelp.contents.investment.linkText")
      )

      contentsLinks.foreach { case (id, messageKey) =>
        val link = doc.select(s"""a[href="#$id"]""")

        link.size() mustBe 1
        link.text() mustBe msgs(messageKey)
      }
    }

    "render the section headings with matching anchor IDs" in {
      val doc = Jsoup.parse(view()(request, msgs).body)

      val sections = Seq(
        ("cis", "cwf1.businessActivitiesHelp.cis.heading"),
        ("share-fishermen", "cwf1.businessActivitiesHelp.shareFishermen.heading"),
        ("land-and-property", "cwf1.businessActivitiesHelp.landAndProperty.heading"),
        ("lloyds-underwriter", "cwf1.businessActivitiesHelp.lloydsUnderwriter.heading"),
        ("directors", "cwf1.businessActivitiesHelp.directors.heading"),
        ("examiners", "cwf1.businessActivitiesHelp.examiners.heading"),
        ("minister-of-religion", "cwf1.businessActivitiesHelp.ministerOfReligion.heading"),
        ("investment", "cwf1.businessActivitiesHelp.investment.heading")
      )

      sections.foreach { case (id, messageKey) =>
        val heading = doc.getElementById(id)

        heading must not be null
        heading.text() mustBe msgs(messageKey)
      }
    }

    "render the CIS and share fishermen content" in {
      val doc = Jsoup.parse(view()(request, msgs).body)

      val paragraphs = doc.select("p.govuk-body").eachText().toArray.toSeq

      paragraphs must contain(msgs("cwf1.businessActivitiesHelp.cis.content"))
      paragraphs must contain(msgs("cwf1.businessActivitiesHelp.shareFishermen.content"))
    }

    "render the land and property content and supplementary pages link" in {
      val doc = Jsoup.parse(view()(request, msgs).body)

      val paragraphs = doc.select("p.govuk-body").eachText().toArray.toSeq

      paragraphs must contain(msgs("cwf1.businessActivitiesHelp.landAndProperty.content.p1"))
      paragraphs must contain(msgs("cwf1.businessActivitiesHelp.landAndProperty.content.p3"))

      val link = doc.select(s"""a[href="${appConfig.supplementaryPages}"]""")

      link.size() mustBe 3
      link.eachText().toArray.toSeq must contain(
        msgs("cwf1.businessActivitiesHelp.landAndProperty.linkText")
      )
    }

    "render the Lloyd's underwriter content" in {
      val doc = Jsoup.parse(view()(request, msgs).body)

      val paragraphs = doc.select("p.govuk-body").eachText().toArray.toSeq

      Seq("p1", "p2", "p3").foreach { paragraph =>
        paragraphs must contain(
          msgs(s"cwf1.businessActivitiesHelp.lloydsUnderwriter.content.$paragraph")
        )
      }
    }

    "render the directors content and link to the SA1 start page" in {
      val doc = Jsoup.parse(view()(request, msgs).body)

      val paragraphs = doc.select("p.govuk-body").eachText().toArray.toSeq

      paragraphs must contain(msgs("cwf1.businessActivitiesHelp.directors.content.p1"))
      paragraphs must contain(msgs("cwf1.businessActivitiesHelp.directors.content.p2"))

      val directorsLink = doc.select(
        s"""a[href="${controllers.sa1.routes.StartController.onPageLoad().url}"]"""
      )

      directorsLink.size() mustBe 2
      directorsLink.eachText().toArray.toSeq must contain(
        msgs("cwf1.businessActivitiesHelp.directors.content.linkText")
      )
      directorsLink.eachText().toArray.toSeq must contain(
        msgs("cwf1.businessActivitiesHelp.examiners.content.linkText")
      )
    }

    "render the examiners content and bullet list" in {
      val doc = Jsoup.parse(view()(request, msgs).body)

      val paragraphs = doc.select("p.govuk-body").eachText().toArray.toSeq

      paragraphs must contain(msgs("cwf1.businessActivitiesHelp.examiners.content.p1"))
      paragraphs must contain(msgs("cwf1.businessActivitiesHelp.examiners.content.p2"))
      paragraphs must contain(msgs("cwf1.businessActivitiesHelp.examiners.content.p3"))

      val bulletKeys = Seq(
        "b1",
        "b2",
        "b3",
        "b4"
      )

      val listItems = doc.select("li").eachText().toArray.toSeq

      bulletKeys.foreach { key =>
        listItems must contain(
          msgs(s"cwf1.businessActivitiesHelp.examiners.content.$key")
        )
      }
    }

    "render the minister of religion content and supplementary pages link" in {
      val doc = Jsoup.parse(view()(request, msgs).body)

      val paragraphs = doc.select("p.govuk-body").eachText().toArray.toSeq

      paragraphs must contain(msgs("cwf1.businessActivitiesHelp.ministerOfReligion.content.p1"))
      paragraphs must contain(msgs("cwf1.businessActivitiesHelp.ministerOfReligion.content.p2"))

      val link = doc.select(s"""a[href="${appConfig.supplementaryPages}"]""")

      link.size() mustBe 3
      link.eachText().toArray.toSeq must contain(
        msgs("cwf1.businessActivitiesHelp.ministerOfReligion.content.linkText")
      )
    }

    "render the investment content and supplementary pages link" in {
      val doc = Jsoup.parse(view()(request, msgs).body)

      val paragraphs = doc.select("p.govuk-body").eachText().toArray.toSeq

      Seq("p1", "p2", "p3", "p4").foreach { paragraph =>
        paragraphs must contain(
          msgs(s"cwf1.businessActivitiesHelp.investment.content.$paragraph")
        )
      }

      val link = doc.select(s"""a[href="${appConfig.supplementaryPages}"]""")
      link.size() mustBe 3
      link.eachText().toArray.toSeq must contain(
        msgs("cwf1.businessActivitiesHelp.investment.content.linkText")
      )
    }

  }
}
