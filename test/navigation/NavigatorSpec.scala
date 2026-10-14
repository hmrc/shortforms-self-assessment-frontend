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

package navigation

import base.SpecBase
import controllers.routes
import models.{CheckMode, NormalMode}
import pages.*
import pages.sa1.PreviousLastNamePage

class NavigatorSpec extends SpecBase {

  private val navigator = new Navigator
  private val journeyId = userAnswersId

  "Navigator" - {

    "in Normal mode" - {

      "must go to the next configured route for PreviousLastNamePage" in {
        navigator.nextPage(
          PreviousLastNamePage,
          NormalMode,
          emptyUserAnswers,
          journeyId
        ) mustBe controllers.sa1.routes.EnterPreviousLastNameController
          .onPageLoad(journeyId, NormalMode)
      }

      "must go from a page that does not exist in the route map to Index" in {
        case object UnknownPage extends Page

        navigator.nextPage(
          UnknownPage,
          NormalMode,
          emptyUserAnswers,
          journeyId
        ) mustBe routes.IndexController.onPageLoad()
      }
    }

    "in Check mode" - {

      "must go to the configured edit route for PreviousLastNamePage" in {
        navigator.nextPage(
          PreviousLastNamePage,
          CheckMode,
          emptyUserAnswers,
          journeyId
        ) mustBe controllers.sa1.routes.EnterPreviousLastNameController
          .onPageLoad(journeyId, CheckMode)
      }

      "must go from a page that does not exist in the edit route map to Index" in {
        case object UnknownPage extends Page

        navigator.nextPage(
          UnknownPage,
          CheckMode,
          emptyUserAnswers,
          journeyId
        ) mustBe routes.IndexController.onPageLoad()
      }
    }
  }
}
