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
import models.*
import models.sa1.WhyTaxReturn
import pages.*
import pages.sa1.{AddPreviousLastNamePage, DoYouHaveANinoPage, WhyTaxReturnPage}

class NavigatorSpec extends SpecBase {

  val navigator = new Navigator

  "Navigator" - {

    "in Normal mode" - {

      "must go from a page that doesn't exist in the route map to Index" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, NormalMode, emptyUserAnswers) mustBe routes.IndexController.onPageLoad()
      }

      "when add previous last name page" - {
        "must go from add previous last name page to enter previous last name page when answer is 'yes'" in {
          val userAnswers = emptyUserAnswers.set(AddPreviousLastNamePage, true).success.value
          navigator.nextPage(
            AddPreviousLastNamePage,
            NormalMode,
            userAnswers
          ) mustBe controllers.sa1.routes.EnterPreviousLastNameController
            .onPageLoad(journeyId = userAnswersId, mode = NormalMode)
        }

        // TODO DTR-8691: Redirect to Enter DOB page
        "must go from add previous last name page to date of birth page when answer is 'no'" in {
          val userAnswers = emptyUserAnswers.set(AddPreviousLastNamePage, false).success.value
          navigator.nextPage(
            AddPreviousLastNamePage,
            NormalMode,
            userAnswers
          ) mustBe controllers.sa1.routes.AddPreviousLastNameController
            .onPageLoad(journeyId = userAnswersId, mode = NormalMode)
        }

        "must go from add previous last name page to Index when answer is empty" in {
          val userAnswers = emptyUserAnswers
          navigator.nextPage(AddPreviousLastNamePage, NormalMode, userAnswers) mustBe routes.IndexController
            .onPageLoad()
        }
      }

      "when Do you have a Nino page" - {
        // TODO DTR-8726: Redirect to Enter NI number page
        "must go from Do you have a Nino page to Enter Nino number page when answer is 'yes'" in {
          val userAnswers = emptyUserAnswers.set(DoYouHaveANinoPage, true).success.value
          navigator.nextPage(
            DoYouHaveANinoPage,
            NormalMode,
            userAnswers
          ) mustBe controllers.sa1.routes.DoYouHaveANinoController
            .onPageLoad(journeyId = userAnswersId, mode = NormalMode)
        }

        "must go from Do you have a Nino page to Enter reason(s) for no Nino number page when answer is 'no'" in {
          val userAnswers = emptyUserAnswers.set(DoYouHaveANinoPage, false).success.value
          navigator.nextPage(
            DoYouHaveANinoPage,
            NormalMode,
            userAnswers
          ) mustBe controllers.sa1.routes.NoNinoReasonController
            .onPageLoad(journeyId = userAnswersId, mode = NormalMode)
        }

        "must go from add previous last name page to Index when answer is empty" in {
          val userAnswers = emptyUserAnswers
          navigator.nextPage(DoYouHaveANinoPage, NormalMode, userAnswers) mustBe routes.IndexController.onPageLoad()
        }
      }

      "when Why tax return page" - {
        // TODO: Redirect to the first date input page for the selected reason(s)
        "must go from Why tax return page to the first date input page when reasons are selected" in {
          val userAnswers = emptyUserAnswers
            .set(WhyTaxReturnPage, Set[WhyTaxReturn](WhyTaxReturn.CompanyDirector, WhyTaxReturn.Other))
            .success
            .value

          navigator.nextPage(
            WhyTaxReturnPage,
            NormalMode,
            userAnswers
          ) mustBe controllers.sa1.routes.WhyTaxReturnController
            .onPageLoad(journeyId = userAnswersId, mode = NormalMode)
        }

        "must go from Why tax return page to Index when answer is empty" in {
          navigator.nextPage(WhyTaxReturnPage, NormalMode, emptyUserAnswers) mustBe routes.IndexController
            .onPageLoad()
        }

        "must go from Why tax return page to Index when no reasons are selected" in {
          val userAnswers = emptyUserAnswers.set(WhyTaxReturnPage, Set.empty[WhyTaxReturn]).success.value

          navigator.nextPage(WhyTaxReturnPage, NormalMode, userAnswers) mustBe routes.IndexController
            .onPageLoad()
        }
      }
    }

    "in Check mode" - {

      "must go from a page that doesn't exist in the edit route map to CheckYourAnswers" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, CheckMode, emptyUserAnswers) mustBe routes.CheckYourAnswersController
          .onPageLoad(journeyId = userAnswersId)
      }

      // TODO DTR-8743: Redirect to Check your answers page
      "must go from any sa1 page to checkYourAnswers" in {
        navigator.nextPage(
          AddPreviousLastNamePage,
          CheckMode,
          emptyUserAnswers
        ) mustBe controllers.sa1.routes.AddPreviousLastNameController
          .onPageLoad(journeyId = userAnswersId, mode = CheckMode)
        navigator.nextPage(
          DoYouHaveANinoPage,
          CheckMode,
          emptyUserAnswers
        ) mustBe controllers.sa1.routes.DoYouHaveANinoController.onPageLoad(journeyId = userAnswersId, mode = CheckMode)
        navigator.nextPage(
          WhyTaxReturnPage,
          CheckMode,
          emptyUserAnswers
        ) mustBe controllers.sa1.routes.WhyTaxReturnController.onPageLoad(journeyId = userAnswersId, mode = CheckMode)
      }
    }
  }
}
