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
import forms.sa1.EnterNationalInsuranceNumberFormProvider
import models.Mode
import pages.sa1.EnterNationalInsuranceNumberPage
import play.api.data.Form
import play.api.mvc.{Action, AnyContent}
import repositories.SessionRepository
import views.html.sa1.EnterNationalInsuranceNumberView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class EnterNationalInsuranceNumberController @Inject() (
  val controllerComponents: AppControllerComponents,
  sessionRepository: SessionRepository,
  formProvider: EnterNationalInsuranceNumberFormProvider,
  view: EnterNationalInsuranceNumberView
)(implicit ec: ExecutionContext)
    extends AppController {

  val form: Form[String] = formProvider()

  def onPageLoad(journeyId: String, mode: Mode): Action[AnyContent] =
    SA1DataAction(journeyId) { implicit request =>

      val preparedForm = request.userAnswers.get(EnterNationalInsuranceNumberPage) match {
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
              updatedAnswers <- Future.fromTry(request.userAnswers.set(EnterNationalInsuranceNumberPage, value.trim()))
              _              <- sessionRepository.set(updatedAnswers)
            } yield request.nino match {
              case Some(nino) if nino.trim() == value.trim() =>
                Redirect(controllers.sa1.routes.EnterNationalInsuranceNumberController.onPageLoad(journeyId, mode))
              case None                                      =>
                Redirect(controllers.sa1.routes.EnterNationalInsuranceNumberController.onPageLoad(journeyId, mode))
              case _                                         =>
                BadRequest(
                  view(
                    form.fill(value).withError("value", "sa1.enterNationalInsuranceNumber.ninoCheck.invalid"),
                    journeyId,
                    mode
                  )
                )

            }
        )
    }
}
