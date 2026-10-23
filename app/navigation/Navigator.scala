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
import pages.sa1.{AddPreviousLastNamePage, DoYouHaveANinoPage}

@Singleton
class Navigator @Inject() () {

  private val normalRoutes: Page => UserAnswers => Call = {
    case AddPreviousLastNamePage =>
      ua =>
        ua.get(AddPreviousLastNamePage) match {
          case Some(true) =>
            controllers.sa1.routes.EnterPreviousLastNameController.onPageLoad(ua.id, NormalMode)

          case Some(false) =>
            // TODO DTR-8691: Redirect to Enter DOB page
            controllers.sa1.routes.AddPreviousLastNameController.onPageLoad(ua.id, NormalMode)

          case _ => routes.IndexController.onPageLoad()
        }

    case DoYouHaveANinoPage =>
      ua =>
        ua.get(DoYouHaveANinoPage) match {
          case Some(true) =>
            // TODO DTR-8726: Redirect to Enter NI number page
            controllers.sa1.routes.DoYouHaveANinoController.onPageLoad(ua.id, NormalMode)

          case Some(false) =>
            controllers.sa1.routes.NoNinoReasonController.onPageLoad(ua.id, NormalMode)

          case _ => routes.IndexController.onPageLoad()
        }

    case _ => _ => routes.IndexController.onPageLoad()
  }

  // TODO DTR-8743: Redirect to Check your answers page
  private val checkRouteMap: Page => UserAnswers => Call = {
    case AddPreviousLastNamePage =>
      ua => controllers.sa1.routes.AddPreviousLastNameController.onPageLoad(ua.id, CheckMode)
    case DoYouHaveANinoPage      =>
      ua => controllers.sa1.routes.DoYouHaveANinoController.onPageLoad(ua.id, CheckMode)
    case _                       => ua => controllers.sa1.routes.CheckYourAnswersController.onPageLoad(ua.id)
  }

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = mode match {
    case NormalMode =>
      normalRoutes(page)(userAnswers)
    case CheckMode  =>
      checkRouteMap(page)(userAnswers)
  }
}
