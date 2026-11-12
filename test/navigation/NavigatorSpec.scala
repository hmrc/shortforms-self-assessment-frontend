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
import models.sa1.{PreviousName, WhyTaxReturn}
import pages.*
import pages.cwf1.AuthorisedAgentPage
import pages.sa1.*

import java.time.LocalDate

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

        "must go from add previous last name page to date of birth page when answer is 'no'" in {
          val userAnswers = emptyUserAnswers.set(AddPreviousLastNamePage, false).success.value

          navigator.nextPage(AddPreviousLastNamePage, NormalMode, userAnswers) mustBe
            controllers.sa1.routes.DateOfBirthController.onPageLoad(userAnswersId, NormalMode)
        }

        "must go from add previous last name page to Index when answer is empty" in {
          val userAnswers = emptyUserAnswers
          navigator.nextPage(AddPreviousLastNamePage, NormalMode, userAnswers) mustBe routes.IndexController
            .onPageLoad()
        }
      }

      "when Do you have a Nino page" - {
        "must go from Do you have a Nino page to Enter Nino number page when answer is 'yes'" in {
          val userAnswers = emptyUserAnswers.set(DoYouHaveANinoPage, true).success.value

          navigator.nextPage(DoYouHaveANinoPage, NormalMode, userAnswers) mustBe
            controllers.sa1.routes.EnterNationalInsuranceNumberController.onPageLoad(userAnswersId, NormalMode)
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

        "must go from DoYouHaveANinoPage to Index when answer is empty" in {
          val userAnswers = emptyUserAnswers
          navigator.nextPage(DoYouHaveANinoPage, NormalMode, userAnswers) mustBe routes.IndexController.onPageLoad()
        }
      }

      "when Why tax return page" - {
        "must go to the first selected reason date page" in {
          val userAnswers = emptyUserAnswers
            .set(WhyTaxReturnPage, Set(WhyTaxReturn.CompanyDirector, WhyTaxReturn.Other))
            .success
            .value

          navigator.nextPage(WhyTaxReturnPage, NormalMode, userAnswers) mustBe
            controllers.sa1.routes.DateUntaxedIncomeDirectorController.onPageLoad(userAnswersId, NormalMode)
        }

        "must go to Index page when no reasons are selected" in {
          val userAnswers = emptyUserAnswers
            .set(WhyTaxReturnPage, Set.empty[WhyTaxReturn])
            .success
            .value

          navigator.nextPage(WhyTaxReturnPage, NormalMode, userAnswers) mustBe
            routes.IndexController.onPageLoad()
        }
      }

      "when completing reason date pages" - {

        "must go to Check Your Answers after completing the last selected reason" in {
          val userAnswers = emptyUserAnswers
            .set(WhyTaxReturnPage, Set(WhyTaxReturn.CompanyDirector))
            .success
            .value
            .set(DateUntaxedIncomeDirectorPage, LocalDate.of(2026, 1, 1))
            .success
            .value

          navigator.nextPage(
            DateUntaxedIncomeDirectorPage,
            NormalMode,
            userAnswers
          ) mustBe controllers.sa1.routes.CheckYourAnswersController.onPageLoad(userAnswersId)
        }
      }

      "when authorised agent page" - {
        "must go from authorised agent page to agent details page when answer is 'yes'" in {
          val userAnswers = emptyUserAnswers.set(AuthorisedAgentPage, true).success.value

          navigator.nextPage(AuthorisedAgentPage, NormalMode, userAnswers) mustBe
            controllers.cwf1.routes.AuthorisedAgentController
              .onPageLoad(userAnswersId, NormalMode) // TODO must change controller to route Agent Details controller
        }

        "must go from authorised agent page to Nino number page when answer is 'no'" in {
          val userAnswers = emptyUserAnswers.set(AuthorisedAgentPage, false).success.value
          navigator.nextPage(
            AuthorisedAgentPage,
            NormalMode,
            userAnswers
          ) mustBe controllers.cwf1.routes.AuthorisedAgentController
            .onPageLoad(
              journeyId = userAnswersId,
              mode = NormalMode
            ) // TODO must change controller to route NINO controller
        }

        "must go from authorised agent to Index when answer is empty" in {
          val userAnswers = emptyUserAnswers
          navigator.nextPage(AuthorisedAgentPage, NormalMode, userAnswers) mustBe routes.IndexController.onPageLoad()
        }
      }

    }

    "in Check mode" - {

      "when AddPreviousLastNamePage" - {

        "must go to EnterPreviousLastName when yes is selected and previous last name is missing" in {
          val userAnswers = emptyUserAnswers
            .set(AddPreviousLastNamePage, true)
            .success
            .value

          navigator.nextPage(AddPreviousLastNamePage, CheckMode, userAnswers) mustBe
            controllers.sa1.routes.EnterPreviousLastNameController.onPageLoad(userAnswersId, CheckMode)
        }

        "must go to Check Your Answers when yes is selected and previous last name already exists" in {
          val userAnswers = emptyUserAnswers
            .set(AddPreviousLastNamePage, true)
            .success
            .value
            .set(PreviousLastNamePage, PreviousName("Smith", LocalDate.of(2000, 1, 1)))
            .success
            .value

          navigator.nextPage(AddPreviousLastNamePage, CheckMode, userAnswers) mustBe
            controllers.sa1.routes.CheckYourAnswersController.onPageLoad(userAnswersId)
        }

        "must go to Check Your Answers when no is selected" in {
          val userAnswers = emptyUserAnswers
            .set(AddPreviousLastNamePage, false)
            .success
            .value

          navigator.nextPage(AddPreviousLastNamePage, CheckMode, userAnswers) mustBe
            controllers.sa1.routes.CheckYourAnswersController.onPageLoad(userAnswersId)
        }

        "must go to Index when answer is missing" in {
          navigator.nextPage(AddPreviousLastNamePage, CheckMode, emptyUserAnswers) mustBe
            routes.IndexController.onPageLoad()
        }
      }

      "when DoYouHaveANinoPage" - {

        "must go to Enter NINO when yes is selected and NINO is missing" in {
          val userAnswers = emptyUserAnswers
            .set(DoYouHaveANinoPage, true)
            .success
            .value

          navigator.nextPage(DoYouHaveANinoPage, CheckMode, userAnswers) mustBe
            controllers.sa1.routes.EnterNationalInsuranceNumberController.onPageLoad(userAnswersId, CheckMode)
        }

        "must go to Check Your Answers when yes is selected and NINO already exists" in {
          val userAnswers = emptyUserAnswers
            .set(DoYouHaveANinoPage, true)
            .success
            .value
            .set(EnterNationalInsuranceNumberPage, "QQ123456C")
            .success
            .value

          navigator.nextPage(DoYouHaveANinoPage, CheckMode, userAnswers) mustBe
            controllers.sa1.routes.CheckYourAnswersController.onPageLoad(userAnswersId)
        }

        "must go to No NINO Reason when no is selected and reason is missing" in {
          val userAnswers = emptyUserAnswers
            .set(DoYouHaveANinoPage, false)
            .success
            .value

          navigator.nextPage(DoYouHaveANinoPage, CheckMode, userAnswers) mustBe
            controllers.sa1.routes.NoNinoReasonController.onPageLoad(userAnswersId, CheckMode)
        }

        "must go to Check Your Answers when no is selected and No NINO Reason already exists" in {
          val userAnswers = emptyUserAnswers
            .set(DoYouHaveANinoPage, false)
            .success
            .value
            .set(NoNinoReasonPage, "No Nino")
            .success
            .value

          navigator.nextPage(DoYouHaveANinoPage, CheckMode, userAnswers) mustBe
            controllers.sa1.routes.CheckYourAnswersController.onPageLoad(userAnswersId)
        }

        "must go to Index when answer is missing" in {
          navigator.nextPage(DoYouHaveANinoPage, CheckMode, emptyUserAnswers) mustBe
            routes.IndexController.onPageLoad()
        }
      }

      "when WhyTaxReturnPage" - {

        "must go to the first unanswered selected reason" in {
          val userAnswers = emptyUserAnswers
            .set(
              WhyTaxReturnPage,
              Set(
                WhyTaxReturn.CompanyDirector,
                WhyTaxReturn.UkPropertyIncome,
                WhyTaxReturn.ForeignIncome
              )
            )
            .success
            .value
            .set(DateUntaxedIncomeDirectorPage, LocalDate.of(2026, 1, 1))
            .success
            .value

          navigator.nextPage(WhyTaxReturnPage, CheckMode, userAnswers) mustBe
            controllers.sa1.routes.DateLandPropertyController.onPageLoad(userAnswersId, CheckMode)
        }

        "must go to Check Your Answers when all selected reasons already have answers" in {
          val userAnswers = emptyUserAnswers
            .set(
              WhyTaxReturnPage,
              Set(WhyTaxReturn.CompanyDirector)
            )
            .success
            .value
            .set(DateUntaxedIncomeDirectorPage, LocalDate.of(2026, 1, 1))
            .success
            .value

          navigator.nextPage(WhyTaxReturnPage, CheckMode, userAnswers) mustBe
            controllers.sa1.routes.CheckYourAnswersController.onPageLoad(userAnswersId)
        }

        "must go to Check Your Answers when no reasons are selected" in {
          val userAnswers = emptyUserAnswers
            .set(WhyTaxReturnPage, Set.empty[WhyTaxReturn])
            .success
            .value

          navigator.nextPage(WhyTaxReturnPage, CheckMode, userAnswers) mustBe
            controllers.sa1.routes.CheckYourAnswersController.onPageLoad(userAnswersId)
        }
      }

      "when completing newly selected reason date pages in CheckMode" - {

        "must navigate through all unanswered reasons before returning to Check Your Answers" in {

          val userAnswers = emptyUserAnswers
            .set(
              WhyTaxReturnPage,
              Set(
                WhyTaxReturn.CompanyDirector,
                WhyTaxReturn.UkPropertyIncome,
                WhyTaxReturn.ForeignIncome
              )
            )
            .success
            .value
            .set(DateUntaxedIncomeDirectorPage, LocalDate.of(2026, 1, 1))
            .success
            .value

          navigator.nextPage(
            WhyTaxReturnPage,
            CheckMode,
            userAnswers
          ) mustBe controllers.sa1.routes.DateLandPropertyController.onPageLoad(userAnswersId, CheckMode)

          val updatedAnswers = userAnswers
            .set(DateLandPropertyPage, LocalDate.of(2026, 2, 1))
            .success
            .value

          navigator.nextPage(
            DateLandPropertyPage,
            CheckMode,
            updatedAnswers
          ) mustBe controllers.sa1.routes.TaxableForeignIncomeDateController.onPageLoad(userAnswersId, CheckMode)

          val completedAnswers = updatedAnswers
            .set(TaxableForeignIncomeDatePage, LocalDate.of(2026, 3, 1))
            .success
            .value

          navigator.nextPage(
            TaxableForeignIncomeDatePage,
            CheckMode,
            completedAnswers
          ) mustBe controllers.sa1.routes.CheckYourAnswersController.onPageLoad(userAnswersId)
        }

        "must navigate to the next unanswered reason after completing Trust date" in {
          val userAnswers = emptyUserAnswers
            .set(
              WhyTaxReturnPage,
              Set(WhyTaxReturn.TrustIncome, WhyTaxReturn.IncomeThresholds)
            )
            .success
            .value
            .set(TrustSettlementDatePage, LocalDate.of(2026, 1, 1))
            .success
            .value

          navigator.nextPage(
            TrustSettlementDatePage,
            CheckMode,
            userAnswers
          ) mustBe controllers.sa1.routes.DateAnnualIncomeController.onPageLoad(userAnswersId, CheckMode)
        }
      }

      "when Authorised Agent Page" - {

        "must go to Agent Details page when yes is selected for Authorised Agent" in {
          val userAnswers = emptyUserAnswers
            .set(AuthorisedAgentPage, true)
            .success
            .value

          navigator.nextPage(AuthorisedAgentPage, CheckMode, userAnswers) mustBe
            controllers.cwf1.routes.AuthorisedAgentController
              .onPageLoad(userAnswersId, CheckMode) // TODO make change controller to route Agent Details
        }

        "must go to Check Your Answers when yes is selected and Agent Details already exists" in {
          val userAnswers = emptyUserAnswers
            .set(AuthorisedAgentPage, true)
            .success
            .value
          // .set(AuthorisedAgentPage, true) //TODO must change page to AgentDetails page post agent details page available
          // .success
          // .value

          navigator.nextPage(AuthorisedAgentPage, CheckMode, userAnswers) mustBe
            controllers.cwf1.routes.AuthorisedAgentController
              .onPageLoad(userAnswersId, CheckMode) // TODO make change controller to route CYA page
        }

        "must go to No NINO page when no is selected" in {
          val userAnswers = emptyUserAnswers
            .set(AuthorisedAgentPage, false)
            .success
            .value

          navigator.nextPage(AuthorisedAgentPage, CheckMode, userAnswers) mustBe
            controllers.cwf1.routes.AuthorisedAgentController
              .onPageLoad(userAnswersId, CheckMode) // TODO make change controller to route NINO page
        }

        "must go to Check Your Answers when no is selected and NINO already exists" in {
          val userAnswers = emptyUserAnswers
            .set(AuthorisedAgentPage, false)
            .success
            .value
          /// .set(NiNOPage, "Nino") //TODO must change page to NINO page post NINO page available
          // .success
          // .value

          navigator.nextPage(AuthorisedAgentPage, CheckMode, userAnswers) mustBe
            controllers.cwf1.routes.AuthorisedAgentController
              .onPageLoad(userAnswersId, CheckMode) // TODO make change controller to route CYA page
        }

        "must go to Index when answer is missing" in {
          navigator.nextPage(AuthorisedAgentPage, CheckMode, emptyUserAnswers) mustBe
            routes.IndexController.onPageLoad()
        }
      }

      "must go from an unknown page to Check Your Answers" in {
        case object UnknownPage extends Page

        navigator.nextPage(UnknownPage, CheckMode, emptyUserAnswers) mustBe
          controllers.sa1.routes.CheckYourAnswersController.onPageLoad(userAnswersId)
      }
    }
  }
}
