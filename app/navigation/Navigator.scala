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

package navigation

import javax.inject.{Inject, Singleton}
import play.api.mvc.Call
import controllers.routes
import pages.*
import models.*
import pages.sa1.Sa1NamePage

@Singleton
class Navigator @Inject() () {

  private def normalRoutes(
    page: Page,
    userAnswers: UserAnswers,
    journeyId: String
  ): Call =
    page match {
      case Sa1NamePage =>
        // TODO DTR-8719: Redirect to SA1-002 "Do you have a previous last name?"
        routes.CheckYourAnswersController.onPageLoad(journeyId)

      case _ => routes.IndexController.onPageLoad()
    }

  private def checkRouteMap(
    page: Page,
    journeyId: String
  ): Call =
    page match {
      case Sa1NamePage =>
        routes.CheckYourAnswersController.onPageLoad(journeyId)

      case _ => routes.CheckYourAnswersController.onPageLoad(journeyId)
    }

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers, journeyId: String): Call = mode match {
    case NormalMode =>
      normalRoutes(page, userAnswers, journeyId)

    case CheckMode =>
      checkRouteMap(page, journeyId)
  }
}
