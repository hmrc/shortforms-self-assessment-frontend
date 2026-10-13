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
import forms.PreviousNameFormProvider
import models.PreviousName
import play.api.i18n.I18nSupport
import play.api.mvc.*
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.sa1.EnterPreviousLastNameView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class EnterPreviousLastNameController @Inject()(
                                        override val controllerComponents: MessagesControllerComponents,
                                        identify: IdentifierAction,
                                        formProvider: PreviousNameFormProvider,
                                        view: EnterPreviousLastNameView
                                      )(implicit ec: ExecutionContext)
  extends FrontendBaseController
    with I18nSupport {

  private val form = formProvider()

  def onPageLoad(journeyId: String): Action[AnyContent] =
    identify { implicit request =>
      Ok(view(form, journeyId))
    }

  def onSubmit(journeyId: String): Action[AnyContent] =
    identify.async { implicit request =>
      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, journeyId))),
        previousName =>
          Future.successful(
            Redirect(controllers.sa1.routes.NameController.onPageLoad(journeyId))
          )
      )
    }
}