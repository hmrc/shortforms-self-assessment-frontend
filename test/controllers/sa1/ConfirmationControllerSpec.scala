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
import controllers.MockAppControllerComponents
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{mock, times, verify, verifyNoInteractions, when}
import play.api.i18n.Messages
import play.api.mvc.Request
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import play.twirl.api.Html
import views.html.sa1.ConfirmationView

import java.time.{Clock, Instant, ZoneOffset}

class ConfirmationControllerSpec extends SpecBase {

  "ConfirmationController" - {

    "must render the backend submission time and acknowledgement reference from the submission" in new Setup {
      val result = controller.onPageLoad()(
        FakeRequest().withFlash("submissionReference" -> "ack-123", "submissionTimestamp" -> "2026-11-13T14:18:34.301Z")
      )

      status(result) mustBe OK
      contentAsString(result) mustBe "Confirmation view"

      verify(view).apply(
        eqTo("14:18 on 13 November 2026"),
        eqTo("ack-123")
      )(any[Request[?]], any[Messages])
    }

    "must fall back to the current UK time when the submission timestamp is missing or invalid" in new Setup {
      val missing = controller.onPageLoad()(FakeRequest().withFlash("submissionReference" -> "ack-123"))
      val invalid = controller.onPageLoad()(
        FakeRequest().withFlash("submissionReference" -> "ack-123", "submissionTimestamp" -> "not-a-date")
      )

      status(missing) mustBe OK
      status(invalid) mustBe OK
      verify(view, times(2)).apply(
        eqTo("15:45 on 16 October 2026"),
        eqTo("ack-123")
      )(any[Request[?]], any[Messages])
    }

    "must redirect to the start of SA1 when there is no submission reference, such as after a page refresh" in new Setup {
      val result = controller.onPageLoad()(FakeRequest())

      status(result) mustBe SEE_OTHER
      redirectLocation(result).value mustBe controllers.routes.IndexController.startSA1().url
      verifyNoInteractions(view)
    }
  }

  private class Setup {
    protected val view: ConfirmationView = mock(classOf[ConfirmationView])
    when(view.apply(any[String], any[String])(any[Request[?]], any[Messages]))
      .thenReturn(Html("Confirmation view"))

    protected val controller = new ConfirmationController(
      new MockAppControllerComponents(),
      Clock.fixed(Instant.parse("2026-10-16T14:45:00Z"), ZoneOffset.UTC),
      view
    )
  }
}
