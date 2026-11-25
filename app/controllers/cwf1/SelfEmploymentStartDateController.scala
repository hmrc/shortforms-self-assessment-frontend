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

package controllers.cwf1

import controllers.{AppController, AppControllerComponents}
import forms.cwf1.SelfEmploymentStartDateFormProvider
import models.Mode
import navigation.Cwf1Navigator
import pages.cwf1.SelfEmploymentStartDatePage
import play.api.mvc.{Action, AnyContent}
import repositories.SessionRepository
import views.html.cwf1.SelfEmploymentStartDateView

import javax.inject.Inject
import scala.concurrent.Future

class SelfEmploymentStartDateController @Inject()(
                                                   val controllerComponents: AppControllerComponents,
                                                   formProvider: SelfEmploymentStartDateFormProvider,
                                                   sessionRepository: SessionRepository,
                                                   navigator: Cwf1Navigator,
                                                   view: SelfEmploymentStartDateView
                                      )extends AppController {

  def onPageLoad(journeyId: String, mode: Mode): Action[AnyContent] = CWF1DataAction(journeyId) {
    implicit request =>

      val form = formProvider()

      val preparedForm = request.userAnswers.get(SelfEmploymentStartDatePage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, journeyId, mode))
  }

  def onSubmit(journeyId: String, mode: Mode): Action[AnyContent] = CWF1DataAction(journeyId).async {
    implicit request =>

      val form = formProvider()

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, journeyId, mode))),

        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(SelfEmploymentStartDatePage, value))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(SelfEmploymentStartDatePage, mode, updatedAnswers))
      )
  }
}
