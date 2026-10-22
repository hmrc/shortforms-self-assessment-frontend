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
import models.identityVerification.IvResultType
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import views.html.ErrorTemplate
import org.mockito.ArgumentMatchers.any
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import play.twirl.api.Html
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class IvControllerSpec extends SpecBase {

  "Iv Controller" - {
    ".handleIvFailure" - {
      "must redirect to IV TechnicalIssue page when journey result is TechnicalIssue" in new Setup {
        when(mockIvConnector.getJourneyStatus(any[String])(any[HeaderCarrier]))
          .thenReturn(Future.successful(IvResultType.TechnicalIssue))

        private val result = ivController.handleIvFailure(Some(testJourneyId))(FakeRequest())

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some("/self-assessment-forms/identity-verification/technical-issue")
      }

      "must redirect to IV Other Result page when journey result is not TechnicalIssue" in new Setup {
        when(mockIvConnector.getJourneyStatus(any[String])(any[HeaderCarrier]))
          .thenReturn(Future.successful(IvResultType.OtherIssue))

        private val result = ivController.handleIvFailure(Some(testJourneyId))(FakeRequest())

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some("/self-assessment-forms/identity-verification/other-result")
      }

      "must redirect to Iv Other Result page when journeyId is not supplied" in new Setup {
        private val result =
          ivController.handleIvFailure(None)(FakeRequest())

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some("/self-assessment-forms/identity-verification/other-result")
      }
    }

    ".ivTechnicalIssue" - {
      "must return OK and display the IV TechnicalIssue page" in new Setup {
        private val result = ivController.ivTechnicalIssue()(FakeRequest())

        status(result) mustBe OK
        contentAsString(result) mustEqual mockContent
      }
    }

    ".ivOtherResult" - {
      "must return OK and display the IV Other Result page" in new Setup {
        private val result = ivController.ivOtherResult()(FakeRequest())

        status(result) mustBe OK
        contentAsString(result) mustEqual mockContent
      }
    }

  }

  private class Setup {
    private val mockView: ErrorTemplate = mock[ErrorTemplate]
    protected val mockContent           = "ErrorTemplate"
    when(mockView(any(), any(), any())(any(), any()))
      .thenReturn(Html(mockContent))

    protected val testJourneyId: String = "testJourneyId"

    protected val mockIvConnector: IvConnector = mock[IvConnector]

    protected val ivController = new IvController(
      new MockAppControllerComponents(),
      mockIvConnector,
      mockView
    )
  }
}
