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

import controllers.actions.{IdentifierAction, SA1DataRequiredAction}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.sa1.NameView

import javax.inject.Inject

class NameController @Inject()(
                                identify: IdentifierAction,
                                sa1DataRequiredAction: SA1DataRequiredAction,
                                val controllerComponents: MessagesControllerComponents,
                                view: NameView
                              ) extends FrontendBaseController with I18nSupport {

  def onPageLoad(journeyId: String): Action[AnyContent] = (identify andThen sa1DataRequiredAction(journeyId)) { implicit request =>
      Ok(view())
    }
}
