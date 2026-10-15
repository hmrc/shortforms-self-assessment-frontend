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

import controllers.actions.IdentifierAction
import controllers.routes
import models.JourneyType.SA1
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.StartJourneyService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.sa1.StartView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class StartController @Inject() (
  identify: IdentifierAction,
  val controllerComponents: MessagesControllerComponents,
  startJourneyService: StartJourneyService,
  view: StartView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(): Action[AnyContent] =
    identify { implicit request =>
      Ok(view())
    }

  def onSubmit(): Action[AnyContent] =
    identify.async { implicit request =>
      startJourneyService
        .startJourney(request.userId, SA1)
        .map { journeyId =>
          Redirect(controllers.sa1.routes.Sa1NameController.onPageLoad(journeyId).url)
        }
        .recover { case ex =>
          logger.error("Failed to start SA1 journey", ex)
          Redirect(routes.JourneyRecoveryController.onPageLoad())
        }
    }
}
