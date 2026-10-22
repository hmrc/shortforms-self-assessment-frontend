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

package controllers.identityVerification

import connectors.IvConnector
import com.google.inject.{Inject, Singleton}
import controllers.{AppController, AppControllerComponents}
import models.identityVerification.IvResultType.TechnicalIssue
import play.api.mvc.{Action, AnyContent}
import views.html.ErrorTemplate

import scala.concurrent.Future

@Singleton
class IvController @Inject() (
  val controllerComponents: AppControllerComponents,
  ivConnector: IvConnector,
  errorTemplate: ErrorTemplate
) extends AppController {

  def handleIvFailure(journeyId: Option[String]): Action[AnyContent] = Action.async { implicit request =>
    journeyId.fold(
      Future.successful(
        Redirect(controllers.identityVerification.routes.IvController.ivOtherResult())
      )
    ) { id =>
      ivConnector.getJourneyStatus(id).map {
        case TechnicalIssue => Redirect(controllers.identityVerification.routes.IvController.ivTechnicalIssue())
        case _              => Redirect(controllers.identityVerification.routes.IvController.ivOtherResult())
      }
    }
  }

  def ivTechnicalIssue(): Action[AnyContent] = Action { implicit request =>
    Ok(
      errorTemplate(
        "Identity Verification Technical Issue",
        "IV Technical Issue",
        "This is a technical issue with Identity Verification"
      )
    )
  }

  def ivOtherResult(): Action[AnyContent] = Action { implicit request =>
    Ok(
      errorTemplate(
        "Identity Verification Other Issue",
        "IV Other Issue",
        "This is an issue with Identity Verification"
      )
    )
  }
}
