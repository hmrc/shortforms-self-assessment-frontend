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
import forms.sa1.OtherReasonFormProvider
import models.Mode
import navigation.Navigator
import pages.sa1.OtherReasonPage
import play.api.mvc.*
import repositories.SessionRepository
import views.html.sa1.OtherReasonView

import javax.inject.Inject
import scala.concurrent.Future

class OtherReasonController @Inject() (
  val controllerComponents: AppControllerComponents,
  formProvider: OtherReasonFormProvider,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  view: OtherReasonView
) extends AppController {

  def onPageLoad(journeyId: String, mode: Mode): Action[AnyContent] =
    SA1DataAction(journeyId) { implicit request =>
      val form         = formProvider()
      val preparedForm = request.userAnswers.get(OtherReasonPage).fold(form)(form.fill)
      Ok(view(preparedForm, journeyId, mode))
    }

  def onSubmit(journeyId: String, mode: Mode): Action[AnyContent] =
    SA1DataAction(journeyId).async { implicit request =>
      formProvider()
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, journeyId, mode))),
          answer =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(OtherReasonPage, answer))
              saved          <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(OtherReasonPage, mode, updatedAnswers))
        )
    }
}
