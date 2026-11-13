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
import utils.DateTimeFormats.submittedAtFormat
import views.html.sa1.ConfirmationView

import java.time.{Clock, Instant, LocalDateTime, ZoneId}
import javax.inject.Inject
import scala.util.Try

class ConfirmationController @Inject() (
  val controllerComponents: AppControllerComponents,
  clock: Clock,
  view: ConfirmationView
) extends AppController {

  private val londonTime = ZoneId.of("Europe/London")

  def onPageLoad(): Action[AnyContent] =
    IdentifierAction { implicit request =>
      request.flash.get("submissionReference").filter(_.trim.nonEmpty) match {
        case Some(referenceNumber) =>
          val submittedInstant = request.flash
            .get("submissionTimestamp")
            .flatMap(value => Try(Instant.parse(value)).toOption)
            .getOrElse(clock.instant())
          val submittedAt      = LocalDateTime.ofInstant(submittedInstant, londonTime).format(submittedAtFormat)
          Ok(view(submittedAt, referenceNumber))
        case None                  =>
          Redirect(controllers.routes.IndexController.startSA1())
      }
    }
}
