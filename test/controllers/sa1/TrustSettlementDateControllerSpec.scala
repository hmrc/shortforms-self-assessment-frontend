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
import forms.sa1.TrustSettlementDateFormProvider
import models.{NormalMode, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.sa1.TrustSettlementDatePage
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.{AnyContentAsFormUrlEncoded, Request}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import play.twirl.api.Html
import repositories.SessionRepository
import views.html.sa1.TrustSettlementDateView

import java.time.LocalDate
import scala.concurrent.Future

class TrustSettlementDateControllerSpec extends SpecBase with MockitoSugar {

  private val validDate = LocalDate.of(1990, 5, 10)

  private val validDateData = Map(
    "trustSettlementDate.day"   -> validDate.getDayOfMonth.toString,
    "trustSettlementDate.month" -> validDate.getMonthValue.toString,
    "trustSettlementDate.year"  -> validDate.getYear.toString
  )

  "TrustSettlementDateController" - {

    "return OK when loading the page without a saved date" in new Setup {
      val result = controllerUnderTest.onPageLoad(userAnswersId, NormalMode)(FakeRequest())

      status(result) mustBe OK
      contentAsString(result) mustBe mockContent
    }

    "pre-populate the form when a date has already been saved" in
      new Setup(emptyUserAnswers.set(TrustSettlementDatePage, validDate).success.value) {
        val result = controllerUnderTest.onPageLoad(userAnswersId, NormalMode)(FakeRequest())

        status(result) mustBe OK

        val captor: ArgumentCaptor[Form[LocalDate]] = ArgumentCaptor.forClass(classOf[Form[LocalDate]])
        verify(mockView).apply(captor.capture(), any[String])(any[Request[?]], any[Messages])

        val populatedForm: Form[LocalDate] = captor.getValue

        populatedForm("trustSettlementDate.day").value mustBe Some("10")
        populatedForm("trustSettlementDate.month").value mustBe Some("5")
        populatedForm("trustSettlementDate.year").value mustBe Some("1990")
      }
  }

  "return BAD_REQUEST when the submitted date is invalid" in new Setup {
    val request: FakeRequest[AnyContentAsFormUrlEncoded] =
      FakeRequest(POST, "/").withFormUrlEncodedBody(
        "trustSettlementDate.day"   -> "31",
        "trustSettlementDate.month" -> "2",
        "trustSettlementDate.year"  -> "2000"
      )

    val result = controllerUnderTest.onSubmit(userAnswersId, NormalMode)(request)

    status(result) mustBe BAD_REQUEST
    contentAsString(result) mustBe mockContent
  }

  "save the date and redirect after a valid submission" in new Setup {
    when(sessionRepository.set(any[UserAnswers])).thenReturn(Future.successful(true))
    val request: FakeRequest[AnyContentAsFormUrlEncoded] =
      FakeRequest(POST, "/").withFormUrlEncodedBody(validDateData.toSeq*)

    val result = controllerUnderTest.onSubmit(userAnswersId, NormalMode)(request)

    status(result) mustBe SEE_OTHER
    redirectLocation(result) mustBe Some(
      controllers.sa1.routes.TrustSettlementDateController.onPageLoad(userAnswersId, NormalMode).url
    )

    val captor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
    verify(sessionRepository).set(captor.capture())
    captor.getValue.get(TrustSettlementDatePage) mustBe Some(validDate)
  }

  private class Setup(
    userAnswers: UserAnswers = emptyUserAnswers
  ) {
    protected val mockView: TrustSettlementDateView    = mock
    protected val mockContent                          = "TrustSettlementDateView"
    protected val sessionRepository: SessionRepository = mock
    private val formProvider                           = new TrustSettlementDateFormProvider()

    when(mockView.apply(any[Form[LocalDate]], any[String])(any[Request[?]], any[Messages]))
      .thenReturn(Html(mockContent))

    protected lazy val controllerUnderTest = new TrustSettlementDateController(
      new MockAppControllerComponents(userAnswers),
      formProvider,
      sessionRepository,
      mockView
    )
  }
}
