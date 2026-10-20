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

package controllers

import com.google.inject.Inject
import controllers.actions.{IdentifierAction, SA1DataRequiredAction}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.govuk.summarylist.*
import views.html.CheckYourAnswersView
import models.CheckMode
import pages.sa1.NamePage
import viewmodels.checkAnswers.sa1.NameSummary

class CheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  requireData: SA1DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view: CheckYourAnswersView
) extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(journeyId: String): Action[AnyContent] = (identify andThen requireData(journeyId)) {
    implicit request =>

      val rows = request.userAnswers.get(NamePage).map(NameSummary.row(_, journeyId, CheckMode)).toSeq

      val list = SummaryListViewModel(
        rows = rows
      )

      Ok(view(list))
  }
}
