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

import controllers.routes
import models.*
import models.sa1.WhyTaxReturn
import models.sa1.WhyTaxReturn.*
import pages.*
import pages.sa1.*
import play.api.mvc.Call

import javax.inject.{Inject, Singleton}

@Singleton
class Navigator @Inject() () {

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call =
    mode match {
      case NormalMode =>
        normalRoutes(page)(userAnswers)

      case CheckMode =>
        checkRouteMap(page)(userAnswers)
    }

  private val normalRoutes: Page => UserAnswers => Call = {
    case NamePage                            =>
      ua => controllers.sa1.routes.AddPreviousLastNameController.onPageLoad(ua.id, NormalMode)
    case AddPreviousLastNamePage             =>
      ua =>
        ua.get(AddPreviousLastNamePage) match {
          case Some(true)  =>
            controllers.sa1.routes.EnterPreviousLastNameController.onPageLoad(ua.id, NormalMode)
          case Some(false) =>
            controllers.sa1.routes.DateOfBirthController.onPageLoad(ua.id, NormalMode)
          case _           =>
            routes.IndexController.onPageLoad()
        }
    case PreviousLastNamePage                =>
      ua => controllers.sa1.routes.DateOfBirthController.onPageLoad(ua.id, NormalMode)
    case DateOfBirthPage                     =>
      ua => controllers.sa1.routes.ContactDetailsController.onPageLoad(ua.id, NormalMode)
    case MoveAddressPage                     =>
      ua => controllers.sa1.routes.DoYouHaveANinoController.onPageLoad(ua.id, NormalMode)
    case ContactDetailsPage                  =>
      ua => controllers.sa1.routes.AddressLookupController.start(ua.id)
    case DoYouHaveANinoPage                  =>
      ua =>
        ua.get(DoYouHaveANinoPage) match {
          case Some(true)  =>
            controllers.sa1.routes.EnterNationalInsuranceNumberController.onPageLoad(ua.id, NormalMode)
          case Some(false) =>
            controllers.sa1.routes.NoNinoReasonController.onPageLoad(ua.id, NormalMode)
          case _           =>
            routes.IndexController.onPageLoad()
        }
    case EnterNationalInsuranceNumberPage    =>
      ua => controllers.sa1.routes.WhyTaxReturnController.onPageLoad(ua.id, NormalMode)
    case NoNinoReasonPage                    =>
      ua => controllers.sa1.routes.WhyTaxReturnController.onPageLoad(ua.id, NormalMode)
    case WhyTaxReturnPage                    =>
      ua => nextReasonRoute(None, ua, NormalMode)
    case DateUntaxedIncomeDirectorPage       =>
      ua => nextReasonRoute(Some(CompanyDirector), ua, NormalMode)
    case DateLandPropertyPage                =>
      ua => nextReasonRoute(Some(UkPropertyIncome), ua, NormalMode)
    case TaxableForeignIncomeDatePage        =>
      ua => nextReasonRoute(Some(ForeignIncome), ua, NormalMode)
    case TrustSettlementDatePage             =>
      ua => nextReasonRoute(Some(TrustIncome), ua, NormalMode)
    case DateAnnualIncomePage                =>
      ua => nextReasonRoute(Some(IncomeThresholds), ua, NormalMode)
    case UntaxedIncomeNotInPayeStartDatePage =>
      ua => nextReasonRoute(Some(UntaxedIncome), ua, NormalMode)
    case HighIncomeChildBenefitDatePage      =>
      ua => nextReasonRoute(Some(ChildBenefitCharge), ua, NormalMode)
    case DateCapitalGainsTaxPage             =>
      ua => nextReasonRoute(Some(CapitalGainsTax), ua, NormalMode)
    case OtherReasonPage                     =>
      ua => nextReasonRoute(Some(Other), ua, NormalMode)
    case _                                   =>
      _ => routes.IndexController.onPageLoad()
  }

  private val checkRouteMap: Page => UserAnswers => Call = {
    case AddPreviousLastNamePage => addPreviousLastNameCheckRoute
    case DoYouHaveANinoPage      => doYouHaveANinoCheckRoute
    case WhyTaxReturnPage        =>
      ua => nextUnansweredReasonRoute(ua)
    case DateUntaxedIncomeDirectorPage | DateLandPropertyPage | TaxableForeignIncomeDatePage | TrustSettlementDatePage |
        DateAnnualIncomePage | UntaxedIncomeNotInPayeStartDatePage | HighIncomeChildBenefitDatePage |
        DateCapitalGainsTaxPage | OtherReasonPage =>
      ua => nextUnansweredReasonRoute(ua)
    case _                       =>
      checkYourAnswersRoute
  }

  private def checkYourAnswersRoute(ua: UserAnswers): Call =
    controllers.sa1.routes.CheckYourAnswersController.onPageLoad(ua.id)

  private def addPreviousLastNameCheckRoute(ua: UserAnswers): Call =
    ua.get(AddPreviousLastNamePage) match {
      case Some(true) if ua.get(PreviousLastNamePage).isEmpty =>
        controllers.sa1.routes.EnterPreviousLastNameController.onPageLoad(ua.id, CheckMode)
      case Some(_)                                            =>
        checkYourAnswersRoute(ua)
      case None                                               =>
        routes.IndexController.onPageLoad()
    }

  private def doYouHaveANinoCheckRoute(ua: UserAnswers): Call =
    ua.get(DoYouHaveANinoPage) match {
      case Some(true) if ua.get(EnterNationalInsuranceNumberPage).isEmpty =>
        controllers.sa1.routes.EnterNationalInsuranceNumberController.onPageLoad(ua.id, CheckMode)
      case Some(false) if ua.get(NoNinoReasonPage).isEmpty                =>
        controllers.sa1.routes.NoNinoReasonController.onPageLoad(ua.id, CheckMode)
      case Some(_)                                                        =>
        checkYourAnswersRoute(ua)
      case None                                                           =>
        routes.IndexController.onPageLoad()
    }

  private def reasonRoute(reason: WhyTaxReturn, journeyId: String, mode: Mode): Call =
    reason match {
      case CompanyDirector    =>
        controllers.sa1.routes.DateUntaxedIncomeDirectorController.onPageLoad(journeyId, mode)
      case UkPropertyIncome   =>
        controllers.sa1.routes.DateLandPropertyController.onPageLoad(journeyId, mode)
      case ForeignIncome      =>
        controllers.sa1.routes.TaxableForeignIncomeDateController.onPageLoad(journeyId, mode)
      case TrustIncome        =>
        controllers.sa1.routes.TrustSettlementDateController.onPageLoad(journeyId, mode)
      case IncomeThresholds   =>
        controllers.sa1.routes.DateAnnualIncomeController.onPageLoad(journeyId, mode)
      case UntaxedIncome      =>
        controllers.sa1.routes.UntaxedIncomeNotInPayeStartDateController.onPageLoad(journeyId, mode)
      case ChildBenefitCharge =>
        controllers.sa1.routes.HighIncomeChildBenefitDateController.onPageLoad(journeyId, mode)
      case CapitalGainsTax    =>
        controllers.sa1.routes.DateCapitalGainsTaxController.onPageLoad(journeyId, mode)
      case Other              =>
        controllers.sa1.routes.OtherReasonController.onPageLoad(journeyId, mode)
    }

  private def orderedSelectedReasons(userAnswers: UserAnswers): Seq[WhyTaxReturn] = {
    val selectedReasons = userAnswers.get(WhyTaxReturnPage).getOrElse(Set.empty)

    WhyTaxReturn.values.filter(selectedReasons.contains)
  }

  private def nextReasonRoute(
    currentReason: Option[WhyTaxReturn],
    userAnswers: UserAnswers,
    mode: Mode
  ): Call = {

    val selectedReasons = orderedSelectedReasons(userAnswers)

    val remainingReasons = currentReason match {
      case Some(reason) =>
        selectedReasons.dropWhile(_ != reason).drop(1)
      case None         =>
        selectedReasons
    }
    remainingReasons.headOption
      .map(reason => reasonRoute(reason, userAnswers.id, mode))
      .getOrElse {
        currentReason match {
          case Some(_) => checkYourAnswersRoute(userAnswers)
          case None    => routes.IndexController.onPageLoad()
        }
      }
  }

  private def hasReasonAnswer(
    reason: WhyTaxReturn,
    userAnswers: UserAnswers
  ): Boolean =
    reason match {
      case CompanyDirector    =>
        userAnswers.get(DateUntaxedIncomeDirectorPage).isDefined
      case UkPropertyIncome   =>
        userAnswers.get(DateLandPropertyPage).isDefined
      case ForeignIncome      =>
        userAnswers.get(TaxableForeignIncomeDatePage).isDefined
      case TrustIncome        =>
        userAnswers.get(TrustSettlementDatePage).isDefined
      case IncomeThresholds   =>
        userAnswers.get(DateAnnualIncomePage).isDefined
      case UntaxedIncome      =>
        userAnswers.get(UntaxedIncomeNotInPayeStartDatePage).isDefined
      case ChildBenefitCharge =>
        userAnswers.get(HighIncomeChildBenefitDatePage).isDefined
      case CapitalGainsTax    =>
        userAnswers.get(DateCapitalGainsTaxPage).isDefined
      case Other              =>
        userAnswers.get(OtherReasonPage).isDefined
    }

  private def nextUnansweredReasonRoute(userAnswers: UserAnswers): Call = {
    val selectedReasons = orderedSelectedReasons(userAnswers)

    selectedReasons
      .find(reason => !hasReasonAnswer(reason, userAnswers))
      .map(reason => reasonRoute(reason, userAnswers.id, CheckMode))
      .getOrElse(checkYourAnswersRoute(userAnswers))
  }
}
