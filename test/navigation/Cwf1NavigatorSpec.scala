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
import models.*
import pages.*
import pages.cwf1.{AgentDetailsPage, AreYouAnAgentPage, ContactDetailsPage, DoYouHaveAPreviousLastNamePage, EnterUTRPage, HaveYouRegisteredForSelfAssessmentPage}

class Cwf1NavigatorSpec extends SpecBase {

  val navigator = new Cwf1Navigator

  "Cwf1Navigator" - {

    "in Normal mode" - {

      "must go from a page that doesn't exist in the route map to Index" in {
        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, NormalMode, emptyUserAnswers) mustBe controllers.cwf1.routes.StartController
          .onPageLoad()
      }

      "when authorised agent page" - {
        "must go from authorised agent page to agent details page when answer is 'yes'" in {
          val userAnswers = emptyUserAnswers.set(AreYouAnAgentPage, true).success.value

          navigator.nextPage(AreYouAnAgentPage, NormalMode, userAnswers) mustBe
            controllers.cwf1.routes.AgentDetailsController
              .onPageLoad(userAnswersId, NormalMode)
        }

        "must go from authorised agent page to Nino number page when answer is 'no'" in {
          val userAnswers = emptyUserAnswers.set(AreYouAnAgentPage, false).success.value
          navigator.nextPage(
            AreYouAnAgentPage,
            NormalMode,
            userAnswers
          ) mustBe controllers.cwf1.routes.AreYouAnAgentController
            .onPageLoad(
              journeyId = userAnswersId,
              mode = NormalMode
            ) // TODO must change controller to route NINO controller
        }

        "must go from authorised agent to Index when answer is empty" in {
          val userAnswers = emptyUserAnswers
          navigator.nextPage(AreYouAnAgentPage, NormalMode, userAnswers) mustBe controllers.cwf1.routes.StartController
            .onPageLoad()
        }
      }

      "when have you registered before page" - {
        "must go from have you registered before to enter utr when answer is 'yes'" in {
          val userAnswers = emptyUserAnswers.set(HaveYouRegisteredForSelfAssessmentPage, true).success.value
          navigator.nextPage(
            HaveYouRegisteredForSelfAssessmentPage,
            NormalMode,
            userAnswers
          ) mustBe controllers.cwf1.routes.EnterUTRController
            .onPageLoad(journeyId = userAnswersId, mode = NormalMode)
        }

        "must go from have you registered before page to have you registered before when answer is 'no'" in {
          val userAnswers = emptyUserAnswers.set(HaveYouRegisteredForSelfAssessmentPage, false).success.value

          navigator.nextPage(HaveYouRegisteredForSelfAssessmentPage, NormalMode, userAnswers) mustBe
            controllers.cwf1.routes.HaveYouRegisteredForSelfAssessmentController.onPageLoad(userAnswersId, NormalMode)
        }

        "must go from have you registered before page to Index when answer is empty" in {
          val userAnswers = emptyUserAnswers
          navigator.nextPage(
            HaveYouRegisteredForSelfAssessmentPage,
            NormalMode,
            userAnswers
          ) mustBe controllers.cwf1.routes.StartController.onPageLoad()
        }
      }

      "when do you have a previous last name page" - {
        "must go to the previous last name form when answer is 'yes'" in {
          val userAnswers = emptyUserAnswers.set(DoYouHaveAPreviousLastNamePage, true).success.value

          navigator.nextPage(DoYouHaveAPreviousLastNamePage, NormalMode, userAnswers) mustBe
            controllers.cwf1.routes.DoYouHaveAPreviousLastNameController.onPageLoad(userAnswersId, NormalMode)
        }

        "must go to the previous last name form when answer is 'no'" in {
          val userAnswers = emptyUserAnswers.set(DoYouHaveAPreviousLastNamePage, false).success.value

          navigator.nextPage(DoYouHaveAPreviousLastNamePage, NormalMode, userAnswers) mustBe
            controllers.cwf1.routes.DoYouHaveAPreviousLastNameController.onPageLoad(userAnswersId, NormalMode)
        }

        "must go to Index when answer is missing" in {
          navigator.nextPage(DoYouHaveAPreviousLastNamePage, NormalMode, emptyUserAnswers) mustBe
            controllers.cwf1.routes.StartController.onPageLoad()
        }
      }

      "when agent details page" - {
        // TODO
        "must go from agent details page to country of territory of agent's address page" in {
          navigator.nextPage(AgentDetailsPage, NormalMode, emptyUserAnswers) mustBe
            controllers.cwf1.routes.AgentDetailsController.onPageLoad(userAnswersId, NormalMode)
        }

        "when contact details page" - {
          // TODO
          "must go from contact details page to country of territory of home address page" in {
            navigator.nextPage(ContactDetailsPage, NormalMode, emptyUserAnswers) mustBe
              controllers.cwf1.routes.ContactDetailsController.onPageLoad(userAnswersId, NormalMode)
          }
        }
      }
    }

    "in Check mode" - {

      "when Authorised Agent Page" - {

        "must go to Agent Details page when yes is selected for Authorised Agent" in {
          val userAnswers = emptyUserAnswers
            .set(AreYouAnAgentPage, true)
            .success
            .value

          navigator.nextPage(AreYouAnAgentPage, CheckMode, userAnswers) mustBe
            controllers.cwf1.routes.AreYouAnAgentController
              .onPageLoad(userAnswersId, CheckMode) // TODO make change controller to route Agent Details
        }

        "must go to Check Your Answers when yes is selected and Agent Details already exists" in {
          val userAnswers = emptyUserAnswers
            .set(AreYouAnAgentPage, true)
            .success
            .value
          // .set(AuthorisedAgentPage, true) //TODO must change page to AgentDetails page post agent details page available
          // .success
          // .value

          navigator.nextPage(AreYouAnAgentPage, CheckMode, userAnswers) mustBe
            controllers.cwf1.routes.AreYouAnAgentController
              .onPageLoad(userAnswersId, CheckMode) // TODO make change controller to route CYA page
        }

        "must go to No NINO page when no is selected" in {
          val userAnswers = emptyUserAnswers
            .set(AreYouAnAgentPage, false)
            .success
            .value

          navigator.nextPage(AreYouAnAgentPage, CheckMode, userAnswers) mustBe
            controllers.cwf1.routes.AreYouAnAgentController
              .onPageLoad(userAnswersId, CheckMode) // TODO make change controller to route NINO page
        }

        "must go to Check Your Answers when no is selected and NINO already exists" in {
          val userAnswers = emptyUserAnswers
            .set(AreYouAnAgentPage, false)
            .success
            .value
          /// .set(NiNOPage, "Nino") //TODO must change page to NINO page post NINO page available
          // .success
          // .value

          navigator.nextPage(AreYouAnAgentPage, CheckMode, userAnswers) mustBe
            controllers.cwf1.routes.AreYouAnAgentController
              .onPageLoad(userAnswersId, CheckMode) // TODO make change controller to route CYA page
        }

        "must go to Index when answer is missing" in {
          navigator.nextPage(AreYouAnAgentPage, CheckMode, emptyUserAnswers) mustBe
            controllers.cwf1.routes.StartController.onPageLoad()
        }
      }

      "when Have Registered Before" - {

        "must go to Enter UTR when yes is selected and utr is missing" in {
          val userAnswers = emptyUserAnswers
            .set(HaveYouRegisteredForSelfAssessmentPage, true)
            .success
            .value

          navigator.nextPage(HaveYouRegisteredForSelfAssessmentPage, CheckMode, userAnswers) mustBe
            controllers.cwf1.routes.EnterUTRController.onPageLoad(userAnswersId, CheckMode)
        }

        "must go to Check Your Answers when yes is selected and utr already exists" in {
          val userAnswers = emptyUserAnswers
            .set(HaveYouRegisteredForSelfAssessmentPage, true)
            .success
            .value
            .set(EnterUTRPage, "1234567890")
            .success
            .value

          navigator.nextPage(HaveYouRegisteredForSelfAssessmentPage, CheckMode, userAnswers) mustBe
            controllers.cwf1.routes.AreYouAnAgentController
              .onPageLoad(userAnswersId, CheckMode) // TODO make change controller to route CYA page
        }

        "must go to Check Your Answers when no is selected" in {
          val userAnswers = emptyUserAnswers
            .set(HaveYouRegisteredForSelfAssessmentPage, false)
            .success
            .value

          navigator.nextPage(HaveYouRegisteredForSelfAssessmentPage, CheckMode, userAnswers) mustBe
            controllers.cwf1.routes.AreYouAnAgentController
              .onPageLoad(userAnswersId, CheckMode) // TODO make change controller to route CYA page
        }

        "must go to Index when answer is missing" in {
          navigator.nextPage(HaveYouRegisteredForSelfAssessmentPage, CheckMode, emptyUserAnswers) mustBe
            controllers.cwf1.routes.StartController.onPageLoad()
        }
      }

      "must go from an unknown page to the CWF1 Check Your Answers page" in {
        case object UnknownPage extends Page

        navigator.nextPage(UnknownPage, CheckMode, emptyUserAnswers) mustBe
          controllers.cwf1.routes.AreYouAnAgentController
            .onPageLoad(userAnswersId, CheckMode) // TODO make change controller to route CYA page
      }
    }
  }
}
