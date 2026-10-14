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

import controllers.routes
import models.*
import pages.*
import pages.sa1.PreviousLastNamePage
import play.api.mvc.Call

import javax.inject.{Inject, Singleton}

@Singleton
class Navigator @Inject()() {

  private def normalRoutes(
                            page: Page,
                            userAnswers: UserAnswers,
                            journeyId: String
                          ): Call = {
    page match {
      // TODO Redirect to DOB page
      case PreviousLastNamePage => controllers.sa1.routes.EnterPreviousLastNameController.onPageLoad(journeyId = journeyId, mode = CheckMode)
      case _ => routes.IndexController.onPageLoad()
    }
  }

  private def checkRouteMap(
                             page: Page,
                             journeyId: String
                           ): Call = {
    page match {
      case PreviousLastNamePage =>
        // TODO Redirect to Check your answers page
        controllers.sa1.routes.EnterPreviousLastNameController.onPageLoad(journeyId = journeyId, mode = CheckMode)
      case _ => routes.IndexController.onPageLoad()
    }
  }

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers, journeyId: String): Call = mode match {
    case NormalMode =>
      normalRoutes(page, userAnswers, journeyId)

    case CheckMode =>
      checkRouteMap(page, journeyId)
  }
}
