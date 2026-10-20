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

import com.google.inject.{Inject, Singleton}
import controllers.actions.{DataRequiredAction, IdentifierAction}
import models.JourneyType.SA1
import models.requests.DataRequest
import play.api.http.FileMimeTypes
import play.api.i18n.{I18nSupport, Langs, MessagesApi}
import play.api.mvc.*
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import scala.concurrent.ExecutionContext

abstract class AppController extends FrontendBaseController with I18nSupport {
  override protected val controllerComponents: AppControllerComponents

  protected def IdentifierAction: IdentifierAction = controllerComponents.identify

  protected def SA1DataAction(journeyId: String): ActionBuilder[DataRequest, AnyContent] =
    controllerComponents.identify andThen controllerComponents.getData(journeyId, SA1)

  protected given ExecutionContext = controllerComponents.executionContext
}

@Singleton
class AppControllerComponents @Inject() (
  val messagesActionBuilder: MessagesActionBuilder,
  val actionBuilder: DefaultActionBuilder,
  val parsers: PlayBodyParsers,
  val messagesApi: MessagesApi,
  val langs: Langs,
  val fileMimeTypes: FileMimeTypes,
  val executionContext: ExecutionContext,
  val identify: IdentifierAction,
  val getData: DataRequiredAction
) extends MessagesControllerComponents
