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
import handlers.ErrorHandler
import models.JourneyType.SA1
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent}
import repositories.SessionRepository
import utils.DateTimeFormats.submittedAtFormat
import views.html.sa1.ConfirmationView

import java.time.{LocalDateTime, ZoneId}
import javax.inject.Inject

class ConfirmationController @Inject() (
  val controllerComponents: AppControllerComponents,
  sessionRepository: SessionRepository,
  view: ConfirmationView,
  errorHandler: ErrorHandler
) extends AppController {

  private val londonTime = ZoneId.of("Europe/London")

  def onPageLoad(journeyId: String): Action[AnyContent] =
    IdentifierAction.async { implicit request =>
      sessionRepository
        .get(journeyId, request.userId, SA1)
        .flatMap {
          case Some(userAnswers) =>
            userAnswers.confirmation match {
              case Some(confirmation) =>
                val submittedAt      =
                  LocalDateTime.ofInstant(confirmation.receivedTimestamp, londonTime).format(submittedAtFormat)
                val confirmationView = Ok(view(submittedAt, confirmation.acknowledgementRef))
                val updatedAnswers   = userAnswers.copy(data = Json.obj())
                sessionRepository.set(updatedAnswers).map(_ => confirmationView)
              case _                  =>
                errorHandler.onClientError(request, NOT_FOUND)
            }
          case None              => errorHandler.onClientError(request, NOT_FOUND)
        }
    }
}
