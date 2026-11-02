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

import java.time.LocalDate
import base.SpecBase
import controllers.sa1.HighIncomeChildBenefitDateController
import forms.sa1.HighIncomeChildBenefitDateFormProvider
import models.{NormalMode, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.sa1.HighIncomeChildBenefitDatePage
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.{AnyContentAsFormUrlEncoded, Request, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import play.twirl.api.Html
import repositories.SessionRepository
import views.html.sa1.HighIncomeChildBenefitDateView

import scala.concurrent.Future

class HighIncomeChildBenefitDateControllerSpec extends SpecBase with MockitoSugar {

  private val validDate = LocalDate.of(1990, 5, 10)

  private val validDateData = Map(
    "value.day"   -> validDate.getDayOfMonth.toString,
    "value.month" -> validDate.getMonthValue.toString,
    "value.year"  -> validDate.getYear.toString
  )

  "HighIncomeChildBenefitDate Controller" - {

    "return OK when loading the page without a saved date" in new Setup {
      val result: Future[Result] = controllerUnderTest.onPageLoad(userAnswersId, NormalMode)(FakeRequest())

      status(result) mustBe OK
      contentAsString(result) mustBe mockContent
    }

    "pre-populate the form when a date has already been saved" in
      new Setup(emptyUserAnswers.set(HighIncomeChildBenefitDatePage, validDate).success.value) {
        val result: Future[Result] = controllerUnderTest.onPageLoad(userAnswersId, NormalMode)(FakeRequest())

        status(result) mustBe OK

        val captor: ArgumentCaptor[Form[LocalDate]] = ArgumentCaptor.forClass(classOf[Form[LocalDate]])
        verify(mockView).apply(captor.capture(), any[String], any)(any[Request[?]], any[Messages])

        val populatedForm: Form[LocalDate] = captor.getValue

        populatedForm("value.day").value mustBe Some("10")
        populatedForm("value.month").value mustBe Some("5")
        populatedForm("value.year").value mustBe Some("1990")
      }
  }

  "return BAD_REQUEST when the submitted date is invalid" in new Setup {
    val request: FakeRequest[AnyContentAsFormUrlEncoded] =
      FakeRequest(POST, "/").withFormUrlEncodedBody(
        "value.day"   -> "31",
        "value.month" -> "2",
        "value.year"  -> "2000"
      )

    val result: Future[Result] = controllerUnderTest.onSubmit(userAnswersId, NormalMode)(request)

    status(result) mustBe BAD_REQUEST
    contentAsString(result) mustBe mockContent
  }

  "save the date and redirect after a valid submission" in new Setup {
    when(sessionRepository.set(any[UserAnswers])).thenReturn(Future.successful(true))
    val request: FakeRequest[AnyContentAsFormUrlEncoded] =
      FakeRequest(POST, "/").withFormUrlEncodedBody(validDateData.toSeq*)

    val result: Future[Result] = controllerUnderTest.onSubmit(userAnswersId, NormalMode)(request)

    status(result) mustBe SEE_OTHER
    redirectLocation(result) mustBe Some(
      controllers.sa1.routes.HighIncomeChildBenefitDateController.onPageLoad(userAnswersId, NormalMode).url
    ) // TODO make change with wiring ticket

    val captor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
    verify(sessionRepository).set(captor.capture())
    captor.getValue.get(HighIncomeChildBenefitDatePage) mustBe Some(validDate)
  }

  private class Setup(
    userAnswers: UserAnswers = emptyUserAnswers
  ) {
    protected val mockView: HighIncomeChildBenefitDateView = mock
    protected val mockContent                              = "HighIncomeChildBenefitDateView"
    protected val sessionRepository: SessionRepository     = mock
    private val formProvider                               = new HighIncomeChildBenefitDateFormProvider

    when(mockView.apply(any[Form[LocalDate]], any[String], any)(any[Request[?]], any[Messages]))
      .thenReturn(Html(mockContent))

    protected lazy val controllerUnderTest = new HighIncomeChildBenefitDateController(
      new MockAppControllerComponents(userAnswers),
      sessionRepository,
      formProvider,
      mockView
    )
  }
}
