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

import java.time.LocalDate
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
            controllers.cwf1.routes.NameController
              .onPageLoad(ua.id, NormalMode)
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
            controllers.cwf1.routes.DateOfBirthController
              .onPageLoad(ua.id, NormalMode)
          case _           =>
            controllers.cwf1.routes.StartController.onPageLoad()
        }
    case EnterUTRPage                           =>
      ua => controllers.cwf1.routes.NameController.onPageLoad(ua.id, NormalMode)
    case HaveYouComeFromNonEuCountryPage        =>
      ua =>
        controllers.cwf1.routes.HaveYouComeFromNonEuCountryController
          .onPageLoad(ua.id, NormalMode) // TODO Business name controller
    case NamePage                               =>
      ua => controllers.cwf1.routes.DoYouHaveAPreviousLastNameController.onPageLoad(ua.id, NormalMode)
    case MoveAddressPage                        =>
      ua =>
        ua.get(MoveAddressPage) match {
          case Some(moveDate) if !LocalDate.parse(moveDate.toString).isBefore(LocalDate.now().minusYears(3)) =>
            controllers.cwf1.routes.AreYouAnAgentController // TODO change to previous address
              .onPageLoad(ua.id, NormalMode)
          case Some(moveDate)                                                                                =>
            controllers.cwf1.routes.EnterUTRController // TODO change to uk resident controller
              .onPageLoad(ua.id, NormalMode)
          case _                                                                                             =>
            controllers.cwf1.routes.StartController.onPageLoad()
        }
    case DoYouHaveABusinessNamePage             =>
      ua =>
        ua.get(DoYouHaveABusinessNamePage) match {
          case Some(true)  =>
            controllers.cwf1.routes.DoYouHaveABusinessNameController
              .onPageLoad(ua.id, NormalMode) // TODO change to enter business name
          case Some(false) =>
            controllers.cwf1.routes.DoYouHaveABusinessNameController
              .onPageLoad(ua.id, NormalMode) // TODO change to self employed work
          case _           =>
            controllers.cwf1.routes.StartController.onPageLoad()
        }
    case ContactDetailsPage                     =>
      ua =>
        controllers.cwf1.routes.ContactDetailsController
          .onPageLoad(ua.id, NormalMode) // TODO: Redirect to Country of Territory of Home Address Page
    case SelfEmploymentStartDatePage            =>
      ua => controllers.cwf1.routes.SelfEmploymentStartDateController.onPageLoad(ua.id, NormalMode) // TODO
    case AreYouUkResidentPage                   =>
      ua =>
        ua.get(AreYouUkResidentPage) match {
          case Some(true)  =>
            controllers.cwf1.routes.DoYouHaveABusinessNameController.onPageLoad(ua.id, NormalMode)
          case Some(false) =>
            // TODO: Redirect to Have You Come From A Non EU page
            controllers.cwf1.routes.AreYouUkResidentController.onPageLoad(ua.id, NormalMode)
          case _           =>
            controllers.cwf1.routes.StartController.onPageLoad()
        }
    case _                                      =>
      _ => controllers.cwf1.routes.StartController.onPageLoad()
  }

  protected val checkRouteMap: Page => UserAnswers => Call = {
    case AreYouAnAgentPage                      => areYouAnAgentCheckRoute
    case HaveYouRegisteredForSelfAssessmentPage => haveYouRegisteredForSelfAssessmentCheckRoute
    case DoYouHaveABusinessNamePage             => doYouHaveABusinessNameCheckRoute
    case MoveAddressPage                        => moveAddressCheckRoute
    case AreYouUkResidentPage                   => areYouUkResidentCheckRoute
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

  private def moveAddressCheckRoute(ua: UserAnswers): Call =
    ua.get(MoveAddressPage) match {
      case Some(moveDate)
          if !LocalDate.parse(moveDate.toString).isBefore(LocalDate.now().minusYears(3)) && ua
            .get(AreYouAnAgentPage)
            .isEmpty =>
        controllers.cwf1.routes.AreYouAnAgentController
          .onPageLoad(ua.id, CheckMode) // TODO change this controller to previous address Controller
      case Some(moveDate)
          if LocalDate.parse(moveDate.toString).isBefore(LocalDate.now().minusYears(3)) &&
            ua.get(EnterUTRPage).isEmpty => // TODO change this page to UK resident Page
        controllers.cwf1.routes.EnterUTRController
          .onPageLoad(ua.id, CheckMode) // TODO change this controller to UK resident Controller
      case Some(_) =>
        checkYourAnswersRoute(ua)
      case None    =>
        controllers.cwf1.routes.StartController.onPageLoad()
    }

  private def doYouHaveABusinessNameCheckRoute(ua: UserAnswers): Call =
    ua.get(DoYouHaveABusinessNamePage) match {
      case Some(true) if ua.get(EnterUTRPage).isEmpty =>
        controllers.cwf1.routes.EnterUTRController
          .onPageLoad(ua.id, CheckMode) // TODO change to enter business name page and controller
      case Some(_)                                    =>
        checkYourAnswersRoute(ua)
      case None                                       =>
        controllers.cwf1.routes.StartController.onPageLoad()
    }

  private def areYouUkResidentCheckRoute(ua: UserAnswers): Call =
    ua.get(AreYouUkResidentPage) match {
      case Some(false) if ua.get(AreYouAnAgentPage).isEmpty => // TODO: Change to Did you come from the EU answer
        controllers.cwf1.routes.AreYouAnAgentController
          .onPageLoad(ua.id, CheckMode)
      case Some(_)                                          =>
        checkYourAnswersRoute(ua)
      case None                                             =>
        controllers.cwf1.routes.StartController.onPageLoad()
    }
}
