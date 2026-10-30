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
import forms.sa1.DateCapitalGainsTaxFormProvider
import models.{NormalMode, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.sa1.DateCapitalGainsTaxPage
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.Request
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import play.twirl.api.Html
import repositories.SessionRepository
import views.html.sa1.DateCapitalGainsTaxView

import java.time.LocalDate
import scala.concurrent.Future

class DateCapitalGainsTaxControllerSpec extends SpecBase with MockitoSugar {

  private val validAnswer: LocalDate = LocalDate.of(2020, 5, 10)

  "DateCapitalGainsTax Controller" - {
    "onPageLoad" - {
      "must return OK and the correct view for a GET" in new Setup {
        val result = controllerUnderTest.onPageLoad(userAnswersId, NormalMode)(FakeRequest())

        status(result) mustEqual OK
        contentAsString(result) mustEqual mockContent
      }

      "must populate the view correctly on a GET when the question has previously been answered" in new Setup(
        emptyUserAnswers.set(DateCapitalGainsTaxPage, validAnswer).success.value
      ) {
        val result = controllerUnderTest.onPageLoad(userAnswersId, NormalMode)(FakeRequest())

        status(result) mustEqual OK

        val captor: ArgumentCaptor[Form[LocalDate]] = ArgumentCaptor.forClass(classOf[Form[LocalDate]])
        verify(mockView).apply(eqTo(userAnswersId), captor.capture(), any())(any[Request[?]], any[Messages])

        val populatedForm: Form[LocalDate] = captor.getValue
        populatedForm("value.day").value mustBe Some("10")
        populatedForm("value.month").value mustBe Some("5")
        populatedForm("value.year").value mustBe Some("2020")
      }
    }

    "onSubmit" - {
      "must redirect to the next page when valid data is submitted" in new Setup {
        when(mockSessionRepository.set(any[UserAnswers])) thenReturn Future.successful(true)

        val request = FakeRequest()
          .withFormUrlEncodedBody(
            "value.day"   -> validAnswer.getDayOfMonth.toString,
            "value.month" -> validAnswer.getMonthValue.toString,
            "value.year"  -> validAnswer.getYear.toString
          )

        val result = controllerUnderTest.onSubmit(userAnswersId, NormalMode)(request)

        status(result) mustEqual SEE_OTHER
        // TODO: Redirect to next page
        redirectLocation(result).value mustEqual controllers.sa1.routes.DateCapitalGainsTaxController
          .onPageLoad(userAnswersId, NormalMode)
          .url

        val captor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockSessionRepository).set(captor.capture())
        captor.getValue.get(DateCapitalGainsTaxPage) mustBe Some(validAnswer)
      }

      "must return a Bad Request and errors when invalid data is submitted" in new Setup {
        val request = FakeRequest()
          .withFormUrlEncodedBody(("value", "invalid value"))

        val result = controllerUnderTest.onSubmit(userAnswersId, NormalMode)(request)

        status(result) mustEqual BAD_REQUEST
      }
    }
  }

  private class Setup(
    userAnswers: UserAnswers = emptyUserAnswers
  ) {
    protected val mockView: DateCapitalGainsTaxView        = mock
    protected val mockContent                              = "DateCapitalGainsTaxView"
    protected val mockSessionRepository: SessionRepository = mock
    private val formProvider                               = new DateCapitalGainsTaxFormProvider

    when(mockView.apply(any[String], any[Form[LocalDate]], any)(any[Request[?]], any[Messages]))
      .thenReturn(Html(mockContent))

    protected lazy val controllerUnderTest = new DateCapitalGainsTaxController(
      new MockAppControllerComponents(userAnswers),
      mockSessionRepository,
      formProvider,
      mockView
    )
  }
}
