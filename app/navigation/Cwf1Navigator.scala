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

import models.*
import pages.*
import pages.cwf1.*
import play.api.mvc.Call

import javax.inject.{Inject, Singleton}

@Singleton
class Cwf1Navigator @Inject() () extends Navigator {

  protected val normalRoutes: Page => UserAnswers => Call = {
    case AreYouAnAgentPage                      =>
      ua =>
        ua.get(AreYouAnAgentPage) match {
          case Some(true)  =>
            controllers.cwf1.routes.AgentDetailsController
              .onPageLoad(ua.id, NormalMode)
          case Some(false) =>
            controllers.cwf1.routes.AreYouAnAgentController
              .onPageLoad(ua.id, NormalMode) // TODO: make change controller for NI Number Page
          case _           =>
            controllers.cwf1.routes.StartController.onPageLoad()
        }
    case AgentDetailsPage                       =>
      ua =>
        controllers.cwf1.routes.AgentDetailsController
          .onPageLoad(ua.id, NormalMode) // TODO: Redirect to Country of Territory of Agent's Address Page
    case HaveYouRegisteredForSelfAssessmentPage =>
      ua =>
        ua.get(HaveYouRegisteredForSelfAssessmentPage) match {
          case Some(true)  =>
            controllers.cwf1.routes.EnterUTRController
              .onPageLoad(ua.id, NormalMode)
          case Some(false) =>
            controllers.cwf1.routes.HaveYouRegisteredForSelfAssessmentController
              .onPageLoad(ua.id, NormalMode) // TODO NAME page
          case _           =>
            controllers.cwf1.routes.StartController.onPageLoad()
        }
    case DoYouHaveAPreviousLastNamePage         =>
      ua =>
        ua.get(DoYouHaveAPreviousLastNamePage) match {
          case Some(true)  =>
            controllers.cwf1.routes.DoYouHaveAPreviousLastNameController
              .onPageLoad(ua.id, NormalMode)
          case Some(false) =>
            controllers.cwf1.routes.DoYouHaveAPreviousLastNameController
              .onPageLoad(ua.id, NormalMode)
          case _           =>
            controllers.cwf1.routes.StartController.onPageLoad()
        }
    case ContactDetailsPage                     =>
      ua =>
        controllers.cwf1.routes.ContactDetailsController
          .onPageLoad(ua.id, NormalMode) // TODO: Redirect to Country of Territory of Home Address Page
    case _                                      =>
      _ => controllers.cwf1.routes.StartController.onPageLoad()
  }

  protected val checkRouteMap: Page => UserAnswers => Call = {
    case AreYouAnAgentPage                      => areYouAnAgentCheckRoute
    case HaveYouRegisteredForSelfAssessmentPage => haveYouRegisteredForSelfAssessmentCheckRoute
    case _                                      => checkYourAnswersRoute
  }

  private def checkYourAnswersRoute(ua: UserAnswers): Call =
    controllers.cwf1.routes.AreYouAnAgentController
      .onPageLoad(ua.id, CheckMode) // TODO change controller to CYA Page controller

  private def areYouAnAgentCheckRoute(ua: UserAnswers): Call =
    ua.get(AreYouAnAgentPage) match {
      case Some(true) if ua.get(AreYouAnAgentPage).isEmpty  => // TODO change this page to AgentDetails Page
        controllers.cwf1.routes.AreYouAnAgentController
          .onPageLoad(ua.id, CheckMode) // TODO change this controller to AgentDetails Controller
      case Some(false) if ua.get(AreYouAnAgentPage).isEmpty => // TODO change this page to NINO Page
        controllers.cwf1.routes.AreYouAnAgentController
          .onPageLoad(ua.id, CheckMode) // TODO change this controller to NINO Controller
      case Some(_)                                          =>
        checkYourAnswersRoute(ua)
      case None                                             =>
        controllers.cwf1.routes.StartController.onPageLoad()
    }

  private def haveYouRegisteredForSelfAssessmentCheckRoute(ua: UserAnswers): Call =
    ua.get(HaveYouRegisteredForSelfAssessmentPage) match {
      case Some(true) if ua.get(EnterUTRPage).isEmpty =>
        controllers.cwf1.routes.EnterUTRController
          .onPageLoad(ua.id, CheckMode)
      case Some(_)                                    =>
        checkYourAnswersRoute(ua)
      case None                                       =>
        controllers.cwf1.routes.StartController.onPageLoad()
    }
}
