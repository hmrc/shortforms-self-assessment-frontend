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
import forms.sa1.NoNinoReasonFormProvider
import models.Mode
import pages.sa1.NoNinoReasonPage
import play.api.mvc.*
import repositories.SessionRepository
import views.html.sa1.NoNinoReasonView

import javax.inject.Inject
import scala.concurrent.Future

class NoNinoReasonController @Inject() (
  val controllerComponents: AppControllerComponents,
  formProvider: NoNinoReasonFormProvider,
  sessionRepository: SessionRepository,
  view: NoNinoReasonView
) extends AppController {

  def onPageLoad(journeyId: String, mode: Mode): Action[AnyContent] =
    SA1DataAction(journeyId) { implicit request =>
      val form         = formProvider()
      val preparedForm = request.userAnswers.get(NoNinoReasonPage).fold(form)(form.fill)

      Ok(view(preparedForm, journeyId))
    }

  def onSubmit(journeyId: String, mode: Mode): Action[AnyContent] =
    SA1DataAction(journeyId).async { implicit request =>
      formProvider()
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, journeyId))),
          noNinoReason =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(NoNinoReasonPage, noNinoReason))
              _              <- sessionRepository.set(updatedAnswers)
            } yield
            Redirect(controllers.sa1.routes.WhyTaxReturnController.onPageLoad(journeyId, mode).url)
        )
    }
}
