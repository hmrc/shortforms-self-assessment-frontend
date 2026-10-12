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

import controllers.actions.*
import forms.sa1.Sa1AddPreviousLastNameFormProvider
import models.Mode
import navigation.Navigator
import pages.sa1.Sa1AddPreviousLastNamePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.sa1.Sa1AddPreviousLastNameView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class Sa1AddPreviousLastNameController @Inject()(
                                         override val messagesApi: MessagesApi,
                                         sessionRepository: SessionRepository,
                                         navigator: Navigator,
                                         identify: IdentifierAction,
                                         sa1DataRequiredAction: SA1DataRequiredAction,
                                         formProvider: Sa1AddPreviousLastNameFormProvider,
                                         val controllerComponents: MessagesControllerComponents,
                                         view: Sa1AddPreviousLastNameView
                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(journeyId: String, mode: Mode): Action[AnyContent] = (identify andThen sa1DataRequiredAction(journeyId)) {
    implicit request =>
      
      val preparedForm = request.userAnswers.get(Sa1AddPreviousLastNamePage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, journeyId, mode))
  }

  def onSubmit(journeyId: String, mode: Mode): Action[AnyContent] = (identify andThen sa1DataRequiredAction(journeyId)).async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, journeyId, mode))),

        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(Sa1AddPreviousLastNamePage, value))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(Sa1AddPreviousLastNamePage, mode, updatedAnswers))
      )
  }
}
