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

package controllers.sa1

import com.google.inject.Inject
import controllers.{AppController, AppControllerComponents}
import models.UserAnswers
import play.api.i18n.Messages
import play.api.mvc.{Action, AnyContent}
import viewmodels.checkAnswers.AddressJourneySummary
import viewmodels.checkAnswers.sa1.*
import viewmodels.govuk.summarylist.*
import views.html.sa1.CheckYourAnswersView

class CheckYourAnswersController @Inject() (
  val controllerComponents: AppControllerComponents,
  view: CheckYourAnswersView
) extends AppController {

  def onPageLoad(journeyId: String): Action[AnyContent] =
    SA1DataAction(journeyId) { implicit request =>

      val ua = request.userAnswers

      val personalDetailsSummary =
        SummaryListViewModel(rows = personalDetailsRows(ua))

      val applicationDetailsSummary =
        SummaryListViewModel(rows = applicationDetailsRows(ua))

      Ok(
        view(
          journeyId,
          personalDetailsSummary,
          applicationDetailsSummary
        )
      )
    }

  def onSubmit(journeyId: String): Action[AnyContent] =
    SA1DataAction(journeyId) { implicit request =>
      Redirect(controllers.sa1.routes.DeclarationController.onPageLoad(journeyId))
    }

  private def personalDetailsRows(ua: UserAnswers)(implicit messages: Messages) =
    NameSummary.rows(ua) ++
      AddPreviousLastNameSummary.row(ua) ++
      EnterPreviousLastNameSummary.rows(ua) ++
      DateOfBirthSummary.rows(ua) ++
      AddressJourneySummary.rows(ua) ++
      MoveAddressSummary.rows(ua) ++
      ContactDetailsSummary.rows(ua) ++
      DoYouHaveANinoSummary.row(ua) ++
      EnterNationalInsuranceNumberSummary.row(ua) ++
      NoNinoReasonSummary.row(ua)

  private def applicationDetailsRows(ua: UserAnswers)(implicit messages: Messages) =
    WhyTaxReturnSummary.rows(ua)
}
