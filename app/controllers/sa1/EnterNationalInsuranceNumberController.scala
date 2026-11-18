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
import navigation.Navigator
import pages.sa1.{DoYouHaveANinoPage, EnterNationalInsuranceNumberPage}
import play.api.mvc.{Action, AnyContent}
import repositories.SessionRepository
import views.html.sa1.EnterNationalInsuranceNumberView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class EnterNationalInsuranceNumberController @Inject() (
  val controllerComponents: AppControllerComponents,
  sessionRepository: SessionRepository,
  formProvider: EnterNationalInsuranceNumberFormProvider,
  navigator: Navigator,
  view: EnterNationalInsuranceNumberView
)(implicit ec: ExecutionContext)
    extends AppController {

  private val form = formProvider()

  def onPageLoad(journeyId: String, mode: Mode): Action[AnyContent] =
    SA1DataAction(journeyId) { implicit req =>
      if req.userAnswers.get(DoYouHaveANinoPage) contains true then
        val preparedForm = req.userAnswers.get(EnterNationalInsuranceNumberPage) match {
          case None        => form
          case Some(value) => form.fill(value)
        }

        Ok(view(preparedForm, journeyId, mode))
      else Redirect(routes.DoYouHaveANinoController.onPageLoad(journeyId, mode))
    }

  def onSubmit(journeyId: String, mode: Mode): Action[AnyContent] =
    SA1DataAction(journeyId).async { implicit req =>
      if req.userAnswers.get(DoYouHaveANinoPage) contains true then
        form
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, journeyId, mode))),
            value =>
              for {
                updatedAnswers <-
                  Future.fromTry(req.userAnswers.set(EnterNationalInsuranceNumberPage, value.replaceAll("\\s", "")))
              } yield req.nino match {
                case Some(nino) if nino.replaceAll("\\s", "").equalsIgnoreCase(value.replaceAll("\\s", "")) =>
                  sessionRepository.set(updatedAnswers)
                  Redirect(navigator.nextPage(EnterNationalInsuranceNumberPage, mode, updatedAnswers))

                case None =>
                  sessionRepository.set(updatedAnswers)
                  Redirect(navigator.nextPage(EnterNationalInsuranceNumberPage, mode, updatedAnswers))

                case _ =>
                  BadRequest(
                    view(
                      form.fill(value).withError("value", "sa1.enterNationalInsuranceNumber.ninoCheck.invalid"),
                      journeyId,
                      mode
                    )
                  )
              }
          )
      else Future.successful(Redirect(routes.DoYouHaveANinoController.onPageLoad(journeyId, mode)))
    }
}
