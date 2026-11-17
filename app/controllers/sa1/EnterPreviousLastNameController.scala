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
import navigation.Navigator
import pages.sa1.{AddPreviousLastNamePage, PreviousLastNamePage}
import play.api.mvc.*
import repositories.SessionRepository
import views.html.sa1.EnterPreviousLastNameView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class EnterPreviousLastNameController @Inject() (
  val controllerComponents: AppControllerComponents,
  formProvider: PreviousNameFormProvider,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  view: EnterPreviousLastNameView
)(implicit ec: ExecutionContext)
    extends AppController {

  def onPageLoad(journeyId: String, mode: Mode): Action[AnyContent] =
    SA1DataAction(journeyId) { implicit req =>
      if req.userAnswers.get(AddPreviousLastNamePage) contains true then
        val form         = formProvider()
        val preparedForm = req.userAnswers.get(PreviousLastNamePage).fold(form)(form.fill)
        Ok(view(preparedForm, journeyId, mode))
      else Redirect(routes.AddPreviousLastNameController.onPageLoad(journeyId, mode))
    }

  def onSubmit(journeyId: String, mode: Mode): Action[AnyContent] =
    SA1DataAction(journeyId).async { implicit req =>
      if req.userAnswers.get(AddPreviousLastNamePage) contains true then
        formProvider()
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, journeyId, mode))),
            answer =>
              for {
                updatedAnswers <- Future.fromTry(req.userAnswers.set(PreviousLastNamePage, answer))
                saved          <- sessionRepository.set(updatedAnswers)
              } yield Redirect(navigator.nextPage(PreviousLastNamePage, mode, updatedAnswers))
          )
      else Future.successful(Redirect(routes.AddPreviousLastNameController.onPageLoad(journeyId, mode)))
    }
}
