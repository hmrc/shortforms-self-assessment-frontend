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
import controllers.routes
import models.JourneyType.SA1
import org.mockito.Mockito.{mock, when}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.StartJourneyService
import views.html.sa1.StartView

import scala.concurrent.Future

class StartControllerSpec extends SpecBase {

  "Start Controller" - {

    "onPageLoad" - {

      "must return OK and display the start page" in {
        val application = applicationBuilder().build()

        running(application) {
          val request =
            FakeRequest(GET, controllers.sa1.routes.StartController.onPageLoad().url)

          val result = route(application, request).value
          val view   = application.injector.instanceOf[StartView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view()(request, messages(application)).toString
        }
      }
    }

    "onSubmit" - {

      "must start an SA1 journey and redirect to the Name page" in {
        val startJourneyService = mock(classOf[StartJourneyService])

        when(startJourneyService.startJourney(testUserId, SA1))
          .thenReturn(Future.successful(userAnswersId))

        val application = applicationBuilder()
          .overrides(
            bind[StartJourneyService].toInstance(startJourneyService)
          )
          .build()

        running(application) {
          val request =
            FakeRequest(POST, controllers.sa1.routes.StartController.onSubmit().url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            controllers.sa1.routes.Sa1NameController.onPageLoad(userAnswersId).url
        }
      }

      "must redirect to Journey Recovery when starting the journey fails" in {
        val startJourneyService = mock(classOf[StartJourneyService])

        when(startJourneyService.startJourney(testUserId, SA1))
          .thenReturn(Future.failed(new RuntimeException("Journey creation failed")))

        val application = applicationBuilder()
          .overrides(
            bind[StartJourneyService].toInstance(startJourneyService)
          )
          .build()

        running(application) {
          val request =
            FakeRequest(POST, controllers.sa1.routes.StartController.onSubmit().url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }
  }
}
