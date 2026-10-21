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

package controllers

import config.AddressLookupConfiguration
import connectors.AddressLookupConnector
import models.{JourneyType, NormalMode}
import pages.AddressPage
import play.api.Logging
import play.api.mvc.{Action, AnyContent, Call}
import repositories.SessionRepository

import javax.inject.Inject
import scala.concurrent.Future
import scala.util.control.NonFatal

class AddressLookupController @Inject() (
  val controllerComponents: AppControllerComponents,
  connector: AddressLookupConnector,
  configuration: AddressLookupConfiguration,
  sessionRepository: SessionRepository
) extends AppController
    with Logging {
  def start(journeyId: String): Action[AnyContent] =
    initialise(journeyId, routes.AddressLookupController.callback(journeyId))

  def change(journeyId: String): Action[AnyContent] =
    initialise(journeyId, routes.AddressLookupController.callbackChange(journeyId))

  private def initialise(journeyId: String, callback: Call): Action[AnyContent] =
    DataAction(journeyId).async { implicit request =>
      connector.initialise(configuration(callback)).map(Redirect(_)).recover { case NonFatal(ex) =>
        logger.error(s"Failed to initialise the ${request.userAnswers.journeyType} address lookup journey", ex)
        Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      }
    }

  def callback(journeyId: String, id: String): Action[AnyContent] =
    saveAddress(journeyId, id, changing = false)

  def callbackChange(journeyId: String, id: String): Action[AnyContent] =
    saveAddress(journeyId, id, changing = true)

  private def saveAddress(journeyId: String, id: String, changing: Boolean): Action[AnyContent] =
    DataAction(journeyId).async { implicit request =>
      (for {
        address        <- if (id.trim.nonEmpty) {
                            connector.getAddress(id)
                          } else {
                            Future.failed(new IllegalArgumentException("ALF callback is missing its address ID"))
                          }
        updatedAnswers <- Future.fromTry(request.userAnswers.set(AddressPage, address))
        saved          <- sessionRepository.set(updatedAnswers)
      } yield {
        if (!saved) throw new IllegalStateException(s"${request.userAnswers.journeyType} address was not saved")
        if (!changing && request.userAnswers.journeyType == JourneyType.SA1) {
          Redirect( // TODO: change this to the nxt page "when did you move to this address" in the journey once it is implemented
            controllers.sa1.routes.AddPreviousLastNameController.onPageLoad(journeyId, NormalMode)
          )
        } else Redirect(controllers.sa1.routes.CheckYourAnswersController.onPageLoad(journeyId))
      }).recover { case NonFatal(ex) =>
        logger.error(s"Failed to retrieve or save the confirmed ${request.userAnswers.journeyType} address", ex)
        Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      }
    }
}
