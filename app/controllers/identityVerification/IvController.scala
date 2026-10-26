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

import com.google.inject.{Inject, Singleton}
import connectors.IvConnector
import controllers.{AppController, AppControllerComponents}
import handlers.ErrorHandler
import models.identityVerification.IvResultType.TechnicalIssue
import play.api.mvc.{Action, AnyContent}
import views.html.ErrorTemplate

@Singleton
class IvController @Inject() (
  val controllerComponents: AppControllerComponents,
  ivConnector: IvConnector,
  errorTemplate: ErrorTemplate,
  errorHandler: ErrorHandler
) extends AppController {

  def handleIvFailure(journeyId: Option[String]): Action[AnyContent] = Action.async { implicit request =>
    journeyId match
      case None     => errorHandler.onClientError(request, BAD_REQUEST)
      case Some(id) =>
        ivConnector.getJourneyStatus(id).map {
          case TechnicalIssue =>
            Ok(
              errorTemplate(
                "idVerification.technicalIssue.title",
                "idVerification.technicalIssue.heading",
                "idVerification.technicalIssue.message"
              )
            )
          case _              =>
            Ok(
              errorTemplate(
                "idVerification.otherIssue.title",
                "idVerification.otherIssue.heading",
                "idVerification.otherIssue.message"
              )
            )
        }
  }
}
