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

import controllers.{AppController, AppControllerComponents}
import forms.sa1.NameFormProvider
import models.{Mode, NormalMode}
import pages.sa1.NamePage
import play.api.mvc.{Action, AnyContent}
import repositories.SessionRepository
import views.html.sa1.NameView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class NameController @Inject() (
  sessionRepository: SessionRepository,
  formProvider: NameFormProvider,
  val controllerComponents: AppControllerComponents,
  view: NameView
)(implicit ec: ExecutionContext)
    extends AppController {

  private val form = formProvider()

  def onPageLoad(journeyId: String, mode: Mode): Action[AnyContent] =
    SA1DataAction(journeyId) { implicit request =>
      val preparedForm = request.userAnswers.get(NamePage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, journeyId, mode))
    }

  def onSubmit(journeyId: String, mode: Mode): Action[AnyContent] =
    SA1DataAction(journeyId).async { implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, journeyId, mode))),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(NamePage, value))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(
              routes.NameController.onPageLoad(journeyId, NormalMode)
            ) // TODO redirect to next page when implemented
        )
    }
}
