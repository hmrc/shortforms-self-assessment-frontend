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

import com.google.inject.{ImplementedBy, Inject, Singleton}
import models.NormalMode
import models.requests.DataRequest
import models.sa1.WhyTaxReturn
import pages.sa1.WhyTaxReturnPage
import play.api.mvc.{ActionFilter, Result, Results}

import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[ReasonRequiredActionImpl])
abstract class ReasonRequiredAction {
  def apply(reason: WhyTaxReturn): ActionFilter[DataRequest]
}

@Singleton
final class ReasonRequiredActionImpl @Inject() ()(using ec: ExecutionContext)
    extends ReasonRequiredAction
    with Results {

  def apply(requiredReason: WhyTaxReturn): ActionFilter[DataRequest] = new ActionFilter {
    val executionContext: ExecutionContext = ec

    def filter[A](request: DataRequest[A]): Future[Option[Result]] =
      val answeredReasons = request.userAnswers.get(WhyTaxReturnPage) getOrElse Set.empty

      val result =
        if answeredReasons contains requiredReason then None
        else
          val journeyId = request.userAnswers.id
          Some(Redirect(controllers.sa1.routes.WhyTaxReturnController.onPageLoad(journeyId, NormalMode)))

      Future.successful(result)
  }
}
