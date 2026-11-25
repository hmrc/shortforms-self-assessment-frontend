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

import handlers.ErrorHandler
import models.{JourneyType, ServiceProps}
import models.requests.{DataRequest, IdentifierRequest}
import play.api.http.Status.NOT_FOUND
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.Results.Gone
import play.api.mvc.{ActionRefiner, RequestHeader, Result}
import repositories.SessionRepository
import views.html.ResubmissionErrorView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DataRequiredActionImpl @Inject() (
  sessionRepository: SessionRepository,
  errorHandler: ErrorHandler,
  resubmissionErrorView: ResubmissionErrorView,
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
          .flatMap {
            case Some(userAnswers) =>
              userAnswers.confirmation match {
                case Some(confirmation) =>
                  val service = ServiceProps.apply(using request)
                  Future.successful(Left(Gone(resubmissionErrorView(service.landingPage))))
                case None               =>
                  Future.successful(Right(new DataRequest(request, userAnswers)))
              }
            case None              =>
              errorHandler.onClientError(request, NOT_FOUND).map(Left(_))
          }
    }
}

trait DataRequiredAction {
  def apply(journeyId: String, journeyType: JourneyType): ActionRefiner[IdentifierRequest, DataRequest]
}
