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
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.Messages
import play.api.mvc.Request
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import play.twirl.api.Html
import views.html.PageNotFoundView

class PageNotFoundControllerSpec extends SpecBase with MockitoSugar {

  "PageNotFound Controller" - {
    "onPageLoad" - {
      "must return OK and the correct view for a GET" in new Setup {
        val result = controllerUnderTest.onPageLoad()(FakeRequest())

        status(result) mustEqual OK
        contentAsString(result) mustEqual mockContent
      }
    }
  }

  private class Setup {
    protected val mockView: PageNotFoundView = mock
    protected val mockContent                = "PageNotFoundView"

    when(mockView.apply()(any[Request[?]], any[Messages]))
      .thenReturn(Html(mockContent))

    protected lazy val controllerUnderTest = new PageNotFoundController(
      new MockAppControllerComponents(),
      mockView
    )
  }
}
