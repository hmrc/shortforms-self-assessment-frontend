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
import views.html.IndexView

class IndexControllerSpec extends SpecBase with MockitoSugar {
  import org.mockito.ArgumentMatchers.any
  import org.mockito.Mockito.when

  "Index Controller" - {

    "must return OK and the correct view for a GET" in new Setup {
      private val result = controllerUnderTest.onPageLoad()(FakeRequest())

      status(result) mustEqual OK

      contentAsString(result) mustEqual mockContent
    }

    "must redirect to the SA1 start page when startSA1 is invoked" in new Setup {
      private val result = controllerUnderTest.startSA1()(FakeRequest())

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual
        controllers.sa1.routes.StartController.onPageLoad().url
    }

    "must return OK and the correct view for a GET when invoke by startCWF1ST" in new Setup {
      private val result = controllerUnderTest.startCWF1ST()(FakeRequest())

      status(result) mustEqual OK

      contentAsString(result) mustEqual "Hello, CWF1ST!"
    }

    "must return OK and the correct view for a GET when invoke by startCeaseTrading" in new Setup {
      private val result = controllerUnderTest.startCeaseTrading()(FakeRequest())

      status(result) mustEqual OK

      contentAsString(result) mustEqual "Hello, Cease Trading!"
    }
  }

  private class Setup {
    private val mockView: IndexView = mock
    protected val mockContent       = "Index"
    when(mockView.apply()(any, any)) thenReturn play.twirl.api.Html(mockContent)

    protected val controllerUnderTest = new IndexController(new MockAppControllerComponents(), mockView)
  }
}
