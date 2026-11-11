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

package viewmodels.checkAnswers.sa1

import base.SpecBase
import models.CheckMode
import models.sa1.Name
import pages.sa1.NamePage
import play.api.test.Helpers.stubMessages

class NameSummarySpec extends SpecBase {

  private val msgs = stubMessages()

  "NameSummary" - {

    "must return no rows when name answers do not exist" in {
      val result = NameSummary.rows(emptyUserAnswers)(msgs)

      result mustBe empty
    }

    "must return two rows when name answers exist" in {
      val name = Name("Jane", "Doe")

      val userAnswers =
        emptyUserAnswers
          .set(NamePage, name)
          .success
          .value

      val result = NameSummary.rows(userAnswers)(msgs)

      result must have size 2

      result.head.value.content.asHtml.toString must include("Jane")
      result(1).value.content.asHtml.toString   must include("Doe")

      val expectedUrl =
        controllers.sa1.routes.NameController
          .onPageLoad(userAnswersId, CheckMode)
          .url

      result.head.actions.value.items.head.href mustBe expectedUrl
      result(1).actions.value.items.head.href mustBe expectedUrl

      result.head.actions.value.items.head.visuallyHiddenText.value mustBe
        msgs("sa1.name.firstName.change.hidden")

      result(1).actions.value.items.head.visuallyHiddenText.value mustBe
        msgs("sa1.name.lastName.change.hidden")

      result.head.actions.value.items.head.attributes("id") must include("first-name")
      result(1).actions.value.items.head.attributes("id")   must include("last-name")

    }

    "must escape the name so it is displayed as text" in {
      val name = Name("<b>Jane</b>", "Doe")

      val userAnswers =
        emptyUserAnswers
          .set(NamePage, name)
          .success
          .value

      val result = NameSummary.rows(userAnswers)(msgs)

      result.head.value.content.asHtml.toString must include("&lt;b&gt;Jane&lt;/b&gt;")
    }
  }
}
