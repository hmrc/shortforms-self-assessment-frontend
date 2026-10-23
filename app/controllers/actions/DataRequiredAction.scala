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

package controllers.actions

import models.JourneyType
import models.requests.{DataRequest, IdentifierRequest}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.Results.NotFound
import play.api.mvc.{ActionRefiner, RequestHeader, Result}
import repositories.SessionRepository
import views.html.ErrorTemplate

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DataRequiredActionImpl @Inject() (
  sessionRepository: SessionRepository,
  errorTemplate: ErrorTemplate,
  val messagesApi: MessagesApi
)(implicit ec: ExecutionContext)
    extends DataRequiredAction
    with I18nSupport {

  override def apply(journeyId: String, journeyType: JourneyType): ActionRefiner[IdentifierRequest, DataRequest] =
    new ActionRefiner[IdentifierRequest, DataRequest] {

      protected def executionContext: ExecutionContext = ec

      protected def refine[A](request: IdentifierRequest[A]): Future[Either[Result, DataRequest[A]]] =
        given RequestHeader = request

        sessionRepository
          .get(journeyId, request.userId, journeyType)
          .map {
            case Some(userAnswers) => Right(new DataRequest(request, userAnswers))
            case None              => Left(NotFound(errorTemplate("notFound.title", "notFound.heading", "notFound.message")))
          }
    }
}

trait DataRequiredAction {
  def apply(journeyId: String, journeyType: JourneyType): ActionRefiner[IdentifierRequest, DataRequest]
}
