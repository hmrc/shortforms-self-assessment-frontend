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

package controllers.identityVerification

import base.SpecBase
import connectors.IvConnector
import controllers.MockAppControllerComponents
import handlers.ErrorHandler
import models.identityVerification.IvResultType
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.mvc.Results
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import play.twirl.api.Html
import uk.gov.hmrc.http.HeaderCarrier
import views.html.ErrorTemplate

import scala.concurrent.Future

class IvControllerSpec extends SpecBase with Results {

  "Iv Controller" - {
    ".handleIvFailure" - {
      "must show IV TechnicalIssue page when journey result is TechnicalIssue" in new Setup {
        when(mockIvConnector.getJourneyStatus(any[String])(any[HeaderCarrier]))
          .thenReturn(Future.successful(IvResultType.TechnicalIssue))

        private val result = ivController.handleIvFailure(Some(testJourneyId))(FakeRequest())

        status(result) mustBe OK
        verify(mockView).apply(
          eqTo("idVerification.technicalIssue.title"),
          eqTo("idVerification.technicalIssue.heading"),
          eqTo("idVerification.technicalIssue.message")
        )(any, any)
      }

      "must show IV Other Result page when journey result is not TechnicalIssue" in new Setup {
        when(mockIvConnector.getJourneyStatus(any[String])(any[HeaderCarrier]))
          .thenReturn(Future.successful(IvResultType.OtherIssue))

        private val result = ivController.handleIvFailure(Some(testJourneyId))(FakeRequest())

        status(result) mustBe OK
        verify(mockView).apply(
          eqTo("idVerification.otherIssue.title"),
          eqTo("idVerification.otherIssue.heading"),
          eqTo("idVerification.otherIssue.message")
        )(any, any)
      }

      "must respond 400 Bad Request when journeyId is not supplied" in new Setup {
        when(mockErrorHandler.onClientError(any, any, any)) thenReturn Future.successful(BadRequest("No journey ID"))

        private val result = ivController.handleIvFailure(None)(FakeRequest())

        status(result) mustBe BAD_REQUEST
        contentAsString(result) mustBe "No journey ID"
      }
    }
  }

  private class Setup {
    protected val mockView: ErrorTemplate = mock
    private val mockContent               = "ErrorTemplate"
    when(mockView(any(), any(), any())(any(), any()))
      .thenReturn(Html(mockContent))

    protected val mockErrorHandler: ErrorHandler = mock

    protected val testJourneyId: String = "testJourneyId"

    protected val mockIvConnector: IvConnector = mock[IvConnector]

    protected val ivController = new IvController(
      new MockAppControllerComponents(),
      mockIvConnector,
      mockView,
      mockErrorHandler
    )
  }
}
