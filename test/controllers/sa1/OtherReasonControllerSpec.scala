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
import forms.sa1.OtherReasonFormProvider
import models.sa1.OtherReason
import models.{NormalMode, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.sa1.OtherReasonPage
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.{AnyContentAsFormUrlEncoded, Request}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import play.twirl.api.Html
import repositories.SessionRepository
import views.html.sa1.OtherReasonView

import java.time.LocalDate
import scala.concurrent.Future

class OtherReasonControllerSpec extends SpecBase with MockitoSugar {

  private val otherReason = OtherReason(
    reason = "test reason",
    reasonDate = LocalDate.of(2020, 5, 10)
  )

  private val validFormData = Map(
    "reason"           -> otherReason.reason,
    "reasonDate.day"   -> otherReason.reasonDate.getDayOfMonth.toString,
    "reasonDate.month" -> otherReason.reasonDate.getMonthValue.toString,
    "reasonDate.year"  -> otherReason.reasonDate.getYear.toString
  )

  "OtherReasonController" - {

    "return OK when loading the page without saved data" in new Setup {
      val result =
        controllerUnderTest.onPageLoad(userAnswersId, NormalMode)(FakeRequest())

      status(result) mustBe OK
      contentAsString(result) mustBe mockContent
    }

    "pre-populate the form when other reason data has already been saved" in
      new Setup(emptyUserAnswers.set(OtherReasonPage, otherReason).success.value) {

        val result = controllerUnderTest.onPageLoad(userAnswersId, NormalMode)(FakeRequest())

        status(result) mustBe OK

        val captor: ArgumentCaptor[Form[OtherReason]] = ArgumentCaptor.forClass(classOf[Form[OtherReason]])
        verify(mockView).apply(captor.capture(), eqTo(userAnswersId))(any[Request[?]], any[Messages])
        val populatedForm: Form[OtherReason]          = captor.getValue

        populatedForm("reason").value mustBe Some("test reason")
        populatedForm("reasonDate.day").value mustBe Some("10")
        populatedForm("reasonDate.month").value mustBe Some("5")
        populatedForm("reasonDate.year").value mustBe Some("2020")
      }

    "return BAD_REQUEST when the submitted form is invalid" in new Setup {
      val request: FakeRequest[AnyContentAsFormUrlEncoded] =
        FakeRequest().withFormUrlEncodedBody(
          "reason"           -> "",
          "reasonDate.day"   -> "10",
          "reasonDate.month" -> "5",
          "reasonDate.year"  -> "2020"
        )

      val result = controllerUnderTest.onSubmit(userAnswersId, NormalMode)(request)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe mockContent
    }

    "save the submitted data and redirect after a valid submission" in new Setup {
      when(sessionRepository.set(any[UserAnswers])).thenReturn(Future.successful(true))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = FakeRequest().withFormUrlEncodedBody(validFormData.toSeq*)

      val result = controllerUnderTest.onSubmit(userAnswersId, NormalMode)(request)
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(
        controllers.sa1.routes.OtherReasonController.onPageLoad(userAnswersId, NormalMode).url
      )

      val captor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])

      verify(sessionRepository).set(captor.capture())
      captor.getValue.get(OtherReasonPage) mustBe Some(otherReason)
    }
  }

  private class Setup(
    userAnswers: UserAnswers = emptyUserAnswers
  ) {
    protected val mockView: OtherReasonView            = mock
    protected val mockContent                          = "OtherReasonView"
    protected val sessionRepository: SessionRepository = mock

    private val formProvider = new OtherReasonFormProvider()

    when(mockView.apply(any[Form[OtherReason]], any[String])(any[Request[?]], any[Messages]))
      .thenReturn(Html(mockContent))
    protected lazy val controllerUnderTest = new OtherReasonController(
      new MockAppControllerComponents(userAnswers),
      formProvider,
      sessionRepository,
      mockView
    )
  }
}
