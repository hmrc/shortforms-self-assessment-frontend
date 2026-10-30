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
import forms.sa1.PreviousNameFormProvider
import models.Mode
import pages.sa1.PreviousLastNamePage
import play.api.mvc.*
import repositories.SessionRepository
import views.html.sa1.EnterPreviousLastNameView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class EnterPreviousLastNameController @Inject() (
  override val controllerComponents: AppControllerComponents,
  formProvider: PreviousNameFormProvider,
  sessionRepository: SessionRepository,
  view: EnterPreviousLastNameView
)(implicit ec: ExecutionContext)
    extends AppController {

  def onPageLoad(journeyId: String, mode: Mode): Action[AnyContent] =
    SA1DataAction(journeyId) { implicit request =>
      val form         = formProvider()
      val preparedForm = request.userAnswers.get(PreviousLastNamePage).fold(form)(form.fill)
      Ok(view(preparedForm, journeyId))
    }

  def onSubmit(journeyId: String, mode: Mode): Action[AnyContent] =
    SA1DataAction(journeyId).async { implicit request =>
      formProvider()
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, journeyId))),
          answer =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(PreviousLastNamePage, answer))
              saved          <- sessionRepository.set(updatedAnswers)
            } yield Redirect(controllers.sa1.routes.DateOfBirthController.onPageLoad(journeyId, mode))
        )
    }
}
