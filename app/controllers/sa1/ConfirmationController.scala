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
import play.api.mvc.{Action, AnyContent}
import views.html.PageNotFoundView
import views.html.sa1.ConfirmationView

import java.time.format.DateTimeFormatter
import java.time.{LocalDateTime, ZoneId}
import java.util.Locale
import javax.inject.Inject
import scala.concurrent.Future

class ConfirmationController @Inject() (
  val controllerComponents: AppControllerComponents,
  view: ConfirmationView,
  pageNotFoundView: PageNotFoundView
) extends AppController {

  private val submittedAtFormatter = DateTimeFormatter.ofPattern("HH:mm 'on' d MMMM yyyy", Locale.UK)
  private val londonTime           = ZoneId.of("Europe/London")

  def onPageLoad(journeyId: String): Action[AnyContent] =
    SA1DataAction(journeyId).async { implicit request =>
      request.userAnswers.confirmation match {
        case Some(confirmation) =>
          val submittedAt =
            LocalDateTime.ofInstant(confirmation.receivedTimestamp, londonTime).format(submittedAtFormatter)
          Future.successful(Ok(view(submittedAt, confirmation.acknowledgementRef)))
        case _                  =>
          Future.successful(NotFound(pageNotFoundView()))
      }
    }
}
