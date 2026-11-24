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
import models.address.ConfirmedAddress
import models.cwf1.AddressType
import models.{CheckMode, Mode, NormalMode}
import navigation.Cwf1Navigator
import pages.QuestionPage
import pages.cwf1.{AgentAddressPage, HomeAddressPage, PreviousHomeAddressPage}
import play.api.Logging
import play.api.mvc.{Action, AnyContent, Call}
import repositories.SessionRepository

import javax.inject.Inject
import scala.concurrent.Future
import scala.util.Failure

class AddressLookupController @Inject() (
  val controllerComponents: AppControllerComponents,
  connector: AddressLookupConnector,
  configuration: AddressLookupConfiguration,
  sessionRepository: SessionRepository,
  navigator: Cwf1Navigator
) extends AppController
    with Logging {

  private val landingPage: Call = routes.StartController.onPageLoad()

  def start(addressType: AddressType, journeyId: String): Action[AnyContent] =
    initialise(addressType, journeyId, routes.AddressLookupController.callback(addressType, journeyId))

  def change(addressType: AddressType, journeyId: String): Action[AnyContent] =
    initialise(addressType, journeyId, routes.AddressLookupController.callbackChange(addressType, journeyId))

  private def initialise(addressType: AddressType, journeyId: String, callback: Call): Action[AnyContent] =
    CWF1DataAction(journeyId).async { implicit request =>
      connector
        .initialise(configuration(callback, landingPage, addressType.messagePrefix, "service.cwf1st.name"))
        .map(Redirect(_))
        .andThen { case Failure(ex) =>
          logger.error(s"Failed to initialise the CWF1 $addressType address lookup journey", ex)
        }
    }

  def callback(addressType: AddressType, journeyId: String, id: String): Action[AnyContent] =
    saveAddress(addressType, journeyId, id, NormalMode)

  def callbackChange(addressType: AddressType, journeyId: String, id: String): Action[AnyContent] =
    saveAddress(addressType, journeyId, id, CheckMode)

  private def saveAddress(addressType: AddressType, journeyId: String, id: String, mode: Mode) =
    CWF1DataAction(journeyId).async { implicit request =>
      val page = addressPage(addressType)
      (for {
        address        <- if (id.trim.nonEmpty) connector.getAddress(id)
                          else Future.failed(new IllegalArgumentException("ALF callback is missing its address ID"))
        updatedAnswers <- Future.fromTry(request.userAnswers.set(page, address))
        _              <- sessionRepository.set(updatedAnswers)
      } yield Redirect(navigator.nextPage(page, mode, updatedAnswers))).andThen { case Failure(ex) =>
        logger.error(s"Failed to retrieve or save the confirmed CWF1 $addressType address", ex)
      }
    }

  private def addressPage(addressType: AddressType): QuestionPage[ConfirmedAddress] =
    addressType match {
      case AddressType.Agent        => AgentAddressPage
      case AddressType.Home         => HomeAddressPage
      case AddressType.PreviousHome => PreviousHomeAddressPage
    }
}
