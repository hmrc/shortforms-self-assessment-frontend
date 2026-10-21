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

package controllers.sa1

import base.SpecBase
import controllers.{MockAppControllerComponents, routes}
import models.JourneyType.SA1
import models.NormalMode
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.StartJourneyService
import views.html.sa1.StartView

import scala.concurrent.Future

class StartControllerSpec extends SpecBase with MockitoSugar {

  "Start Controller" - {

    "onPageLoad" - {

      "must return OK and display the start page" in new Setup {
        private val result = controllerUnderTest.onPageLoad()(FakeRequest())

        status(result) mustEqual OK
        contentAsString(result) mustEqual mockContent
      }
    }

    "onSubmit" - {

      "must start an SA1 journey and redirect to the Name page" in new Setup {
        when(startJourneyService.startJourney(testUserId, SA1)) thenReturn
          Future.successful(userAnswersId)

        private val result = controllerUnderTest.onSubmit()(FakeRequest())

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.sa1.routes.NameController.onPageLoad(userAnswersId, NormalMode).url
      }

      "must redirect to Journey Recovery when starting the journey fails" in new Setup {
        when(startJourneyService.startJourney(testUserId, SA1)) thenReturn
          Future.failed(new RuntimeException("Journey creation failed"))

        private val result = controllerUnderTest.onSubmit()(FakeRequest())

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }

  private class Setup {
    private val mockView: StartView = mock
    protected val mockContent       = "StartView"
    when(mockView.apply()(any, any)) thenReturn play.twirl.api.Html(mockContent)

    protected val startJourneyService: StartJourneyService = mock

    protected val controllerUnderTest = new StartController(
      new MockAppControllerComponents(),
      startJourneyService,
      mockView
    )
  }
}
