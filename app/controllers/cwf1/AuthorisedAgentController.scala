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
import forms.cwf1.AuthorisedAgentFormProvider
import models.Mode
import navigation.Navigator
import pages.cwf1.AuthorisedAgentPage
import play.api.data.Form
import play.api.mvc.{Action, AnyContent}
import repositories.SessionRepository
import views.html.cwf1.AuthorisedAgentView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AuthorisedAgentController @Inject() (
  sessionRepository: SessionRepository,
  navigator: Navigator,
  formProvider: AuthorisedAgentFormProvider,
  val controllerComponents: AppControllerComponents,
  view: AuthorisedAgentView
)(implicit ec: ExecutionContext)
    extends AppController {

  val form: Form[Boolean] = formProvider()

  def onPageLoad(journeyId: String, mode: Mode): Action[AnyContent] = SA1DataAction(journeyId) { implicit request =>

    val preparedForm = request.userAnswers.get(AuthorisedAgentPage) match {
      case None        => form
      case Some(value) => form.fill(value)
    }

    Ok(view(preparedForm, journeyId, mode))
  }

  def onSubmit(journeyId: String, mode: Mode): Action[AnyContent] = SA1DataAction(journeyId).async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, journeyId, mode))),
        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(AuthorisedAgentPage, value))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(
            controllers.cwf1.routes.AuthorisedAgentController.onPageLoad(journeyId, mode)
          ) // TODO post implementation of other pages.
      )
  }
}
