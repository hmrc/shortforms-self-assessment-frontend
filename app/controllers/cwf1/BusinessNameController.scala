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

import com.google.inject.{Inject, Singleton}
import controllers.{AppController, AppControllerComponents}
import forms.cwf1.BusinessNameFormProvider
import models.Mode
import navigation.Cwf1Navigator
import pages.cwf1.BusinessNamePage
import play.api.mvc.{Action, AnyContent}
import repositories.SessionRepository
import views.html.cwf1.BusinessNameView

import scala.concurrent.Future

@Singleton
class BusinessNameController @Inject() (
  val controllerComponents: AppControllerComponents,
  sessionRepository: SessionRepository,
  formProvider: BusinessNameFormProvider,
  view: BusinessNameView,
  navigator: Cwf1Navigator
) extends AppController {

  private val form = formProvider()

  def onPageLoad(journeyId: String, mode: Mode): Action[AnyContent] = CWF1DataAction(journeyId) { implicit request =>

    val preparedForm = request.userAnswers.get(BusinessNamePage).fold(form)(form.fill)

    Ok(view(preparedForm, journeyId, mode))
  }

  def onSubmit(journeyId: String, mode: Mode): Action[AnyContent] = CWF1DataAction(journeyId).async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, journeyId, mode))),
          value =>
            for
              updatedAnswers <- Future.fromTry(request.userAnswers.set(BusinessNamePage, value))
              _              <- sessionRepository.set(updatedAnswers)
            yield Redirect(navigator.nextPage(BusinessNamePage, mode, request.userAnswers))
        )
  }
}
