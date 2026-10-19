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

import controllers.actions.FakeIdentifierAction
import controllers.routes
import models.JourneyType.SA1
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.Messages
import play.api.mvc.{Request, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import play.twirl.api.Html
import services.StartJourneyService
import views.html.sa1.StartView

import scala.concurrent.{ExecutionContext, Future}

class StartControllerSpec extends AnyWordSpec with Matchers with MockitoSugar {
  private implicit val ec: ExecutionContext = ExecutionContext.global

  private val journeyId = "test-journey-id"
  private val userId = "test-user-id"
  private val mockViewContent = "test view"

  private class Setup {
    protected val startJourneyService: StartJourneyService = mock[StartJourneyService]
    private val view: StartView = mock[StartView]

    private val controllerComponents = stubMessagesControllerComponents()
    private val identifierAction = new FakeIdentifierAction(controllerComponents.parsers)

    protected lazy val controller = new StartController(
        identifierAction,
        controllerComponents,
        startJourneyService,
        view
      )

    when(view.apply()(any[Request[?]], any[Messages])).thenReturn(Html(mockViewContent))
  }

  "StartController" should {

    "return OK and display the start page" in new Setup {
      val result = controller.onPageLoad()(FakeRequest())

      status(result) mustBe OK
      contentAsString(result) mustBe mockViewContent
    }

    "start an SA1 journey and redirect to the Name page" in new Setup {
      when(startJourneyService.startJourney(userId, SA1)).thenReturn(Future.successful(journeyId))

      val result = controller.onSubmit()(FakeRequest(POST, "/"))

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.sa1.routes.NameController.onPageLoad(journeyId).url)
      verify(startJourneyService).startJourney(userId, SA1)
    }

    "redirect to Journey Recovery when starting the journey fails" in new Setup {
      when(startJourneyService.startJourney(userId, SA1)).thenReturn(Future.failed(new RuntimeException("Journey creation failed")))

      val result = controller.onSubmit()(FakeRequest(POST, "/"))

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(routes.JourneyRecoveryController.onPageLoad().url)
    }
  }
}
