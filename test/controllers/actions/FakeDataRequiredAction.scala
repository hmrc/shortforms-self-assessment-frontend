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

import controllers.routes
import models.requests.{DataRequest, IdentifierRequest}
import models.{JourneyType, UserAnswers}
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionRefiner, Result}

import scala.concurrent.{ExecutionContext, Future}

class FakeDataRequiredAction(
                              userAnswers: Option[UserAnswers]
                            )(implicit ec: ExecutionContext) extends DataRequiredAction {

  override def apply(
                      journeyId: String,
                      journeyType: JourneyType
                    ): ActionRefiner[IdentifierRequest, DataRequest] =
    new ActionRefiner[IdentifierRequest, DataRequest] {

      override protected def executionContext: ExecutionContext = ec

      override protected def refine[A](
                                        request: IdentifierRequest[A]
                                      ): Future[Either[Result, DataRequest[A]]] =
        Future.successful {
          userAnswers match {
            case Some(answers) =>
              Right(
                DataRequest(
                  request.request,
                  request.userId,
                  answers
                )
              )

            case _ =>
              Left(
                Redirect(
                  routes.JourneyRecoveryController.onPageLoad()
                )
              )
          }
        }
    }
}
