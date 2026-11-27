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

import base.SpecBase
import controllers.{MockAppControllerComponents, routes}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.cwf1.BusinessActivitiesHelpView

import scala.concurrent.Future

class BusinessActivitiesHelpControllerSpec extends SpecBase with MockitoSugar {

  "BusinessActivitiesHelp Controller" - {

    "onPageLoad" - {

      "must return OK and display the business activities help page" in new Setup {
        private val result = controllerUnderTest.onPageLoad()(FakeRequest())

        status(result) mustEqual OK
        contentAsString(result) mustEqual mockContent
      }
    }

  }

  private class Setup {
    private val mockView: BusinessActivitiesHelpView = mock
    protected val mockContent                        = "BusinessActivitiesHelpView"
    when(mockView.apply()(any, any)) thenReturn play.twirl.api.Html(mockContent)

    protected val controllerUnderTest = new BusinessActivitiesHelpController(
      new MockAppControllerComponents(),
      mockView
    )
  }
}
