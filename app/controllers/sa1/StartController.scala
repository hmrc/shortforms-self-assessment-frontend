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

import controllers.{AppController, AppControllerComponents, routes}
import models.JourneyType.SA1
import models.NormalMode
import play.api.Logging
import play.api.mvc.{Action, AnyContent}
import services.StartJourneyService
import views.html.sa1.StartView

import javax.inject.Inject

class StartController @Inject() (
  val controllerComponents: AppControllerComponents,
  startJourneyService: StartJourneyService,
  view: StartView
) extends AppController
    with Logging {

  def onPageLoad(): Action[AnyContent] =
    IdentifierAction { implicit request =>
      Ok(view())
    }

  def onSubmit(): Action[AnyContent] =
    IdentifierAction.async { implicit request =>
      startJourneyService
        .startJourney(request.userId, SA1)
        .map { journeyId =>
          Redirect(controllers.sa1.routes.NameController.onPageLoad(journeyId, NormalMode).url)
        }
        .recover { case ex =>
          logger.error("Failed to start SA1 journey", ex)
          Redirect(routes.JourneyRecoveryController.onPageLoad())
        }
    }
}
