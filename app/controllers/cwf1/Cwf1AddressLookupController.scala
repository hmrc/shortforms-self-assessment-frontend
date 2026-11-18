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

package controllers.cwf1

import config.AddressLookupConfiguration
import connectors.AddressLookupConnector
import controllers.{AppController, AppControllerComponents}
import models.{CheckMode, Cwf1AddressType, Mode, NormalMode}
import navigation.Navigator
import pages.cwf1.Cwf1AddressPage
import play.api.Logging
import play.api.mvc.{Action, AnyContent, Call}
import repositories.SessionRepository

import javax.inject.Inject
import scala.concurrent.Future
import scala.util.control.NonFatal

class Cwf1AddressLookupController @Inject() (
  val controllerComponents: AppControllerComponents,
  connector: AddressLookupConnector,
  configuration: AddressLookupConfiguration,
  sessionRepository: SessionRepository,
  navigator: Navigator
) extends AppController
    with Logging {

  private val landingPage: Call = routes.StartController.onPageLoad()

  def start(addressType: Cwf1AddressType, journeyId: String): Action[AnyContent] =
    initialise(addressType, journeyId, routes.Cwf1AddressLookupController.callback(addressType, journeyId))

  def change(addressType: Cwf1AddressType, journeyId: String): Action[AnyContent] =
    initialise(addressType, journeyId, routes.Cwf1AddressLookupController.callbackChange(addressType, journeyId))

  private def initialise(addressType: Cwf1AddressType, journeyId: String, callback: Call): Action[AnyContent] =
    CWF1DataAction(journeyId).async { implicit request =>
      connector
        .initialise(configuration(callback, landingPage, addressType.messagePrefix, "service.cwf1st.name"))
        .map(Redirect(_))
        .recover { case NonFatal(ex) =>
          logger.error(s"Failed to initialise the CWF1 $addressType address lookup journey", ex)
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
        }
    }

  def callback(addressType: Cwf1AddressType, journeyId: String, id: String): Action[AnyContent] =
    saveAddress(addressType, journeyId, id, NormalMode)

  def callbackChange(addressType: Cwf1AddressType, journeyId: String, id: String): Action[AnyContent] =
    saveAddress(addressType, journeyId, id, CheckMode)

  private def saveAddress(addressType: Cwf1AddressType, journeyId: String, id: String, mode: Mode) =
    CWF1DataAction(journeyId).async { implicit request =>
      val page = Cwf1AddressPage(addressType)
      (for {
        address        <- if (id.trim.nonEmpty) connector.getAddress(id)
                          else Future.failed(new IllegalArgumentException("ALF callback is missing its address ID"))
        updatedAnswers <- Future.fromTry(request.userAnswers.set(page, address))
        saved          <- sessionRepository.set(updatedAnswers)
      } yield {
        if (!saved) throw new IllegalStateException(s"CWF1 $addressType address was not saved")
        Redirect(navigator.nextPage(page, mode, updatedAnswers))
      }).recover { case NonFatal(ex) =>
        logger.error(s"Failed to retrieve or save the confirmed CWF1 $addressType address", ex)
        Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      }
    }
}
