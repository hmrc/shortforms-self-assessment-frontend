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

package controllers

import base.SpecBase
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.play.bootstrap.binders.RedirectUrl
import views.html.{JourneyRecoveryContinueView, JourneyRecoveryStartAgainView}

class JourneyRecoveryControllerSpec extends SpecBase with MockitoSugar {
  import org.mockito.ArgumentMatchers.{any, eq as eqTo}
  import org.mockito.Mockito.{verify, when}

  "JourneyRecovery Controller" - {

    "when a relative continue Url is supplied" - {

      "must return OK and the continue view" in new Setup {
        private val continueUrl = RedirectUrl("/foo")

        private val result = controllerUnderTest.onPageLoad(Some(continueUrl))(FakeRequest())

        status(result) mustEqual OK
        contentAsString(result) mustEqual mockContinueContent
        verify(mockContinueView).apply(eqTo(continueUrl.unsafeValue))(any, any)
      }
    }

    "when an absolute continue Url is supplied" - {

      "must return OK and the start again view" in new Setup {
        private val continueUrl = RedirectUrl("https://foo.com")

        private val result = controllerUnderTest.onPageLoad(Some(continueUrl))(FakeRequest())

        status(result) mustEqual OK
        contentAsString(result) mustEqual mockStartAgainContent
      }
    }

    "when no continue Url is supplied" - {

      "must return OK and the start again view" in new Setup {
        private val result = controllerUnderTest.onPageLoad(None)(FakeRequest())

        status(result) mustEqual OK
        contentAsString(result) mustEqual mockStartAgainContent
      }
    }
  }

  private class Setup {
    protected val mockContinueView: JourneyRecoveryContinueView = mock
    protected val mockContinueContent                           = "JourneyRecoveryContinue"
    when(mockContinueView.apply(any)(any, any)) thenReturn play.twirl.api.Html(mockContinueContent)

    private val mockStartAgainView: JourneyRecoveryStartAgainView = mock
    protected val mockStartAgainContent                           = "JourneyRecoveryStartAgain"
    when(mockStartAgainView.apply()(any, any)) thenReturn play.twirl.api.Html(mockStartAgainContent)

    protected val controllerUnderTest = new JourneyRecoveryController(
      new MockAppControllerComponents(),
      mockContinueView,
      mockStartAgainView
    )
  }
}
