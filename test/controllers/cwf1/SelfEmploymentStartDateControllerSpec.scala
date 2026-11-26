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

package controllers.cwf1

import base.SpecBase
import controllers.MockAppControllerComponents
import forms.cwf1.SelfEmploymentStartDateFormProvider
import models.{Mode, NormalMode, UserAnswers}
import navigation.{Cwf1Navigator, FakeNavigator}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.cwf1.SelfEmploymentStartDatePage
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.{AnyContentAsFormUrlEncoded, Call, Request, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import play.twirl.api.Html
import repositories.SessionRepository
import views.html.cwf1.SelfEmploymentStartDateView

import java.time.LocalDate
import scala.concurrent.Future

class SelfEmploymentStartDateControllerSpec extends SpecBase with MockitoSugar {
  def onwardRoute = Call("GET", "/foo")

  private val validDate = LocalDate.of(1990, 5, 10)

  private val validDateData = Map(
    "selfEmploymentStartDate.day"   -> "10",
    "selfEmploymentStartDate.month" -> "5",
    "selfEmploymentStartDate.year"  -> "1990"
  )

  "SelfEmploymentStartDateController" - {

    "onPageLoad" - {

      "must return OK when loading the page without a saved date" in new Setup {
        val result = controllerUnderTest.onPageLoad(userAnswersId, NormalMode)(FakeRequest())

        status(result) mustEqual OK
        contentAsString(result) mustEqual mockContent
      }

      "must pre-populate the form when a date has already been saved" in new Setup(
        emptyUserAnswers.set(SelfEmploymentStartDatePage, validDate).success.value
      ) {
        val result: Future[Result] = controllerUnderTest.onPageLoad(userAnswersId, NormalMode)(FakeRequest())

        status(result) mustEqual OK

        val captor: ArgumentCaptor[Form[LocalDate]] = ArgumentCaptor.forClass(classOf[Form[LocalDate]])
        verify(mockView)
          .apply(captor.capture(), eqTo(userAnswersId), eqTo[Mode](NormalMode))(any[Request[?]], any[Messages])

        val populatedForm: Form[LocalDate] = captor.getValue

        populatedForm("selfEmploymentStartDate.day").value mustBe Some("10")
        populatedForm("selfEmploymentStartDate.month").value mustBe Some("5")
        populatedForm("selfEmploymentStartDate.year").value mustBe Some("1990")
      }
    }

    "onSubmit" - {

      "must return BAD_REQUEST when the submitted date is invalid" in new Setup {
        val request: FakeRequest[AnyContentAsFormUrlEncoded] =
          FakeRequest(POST, "/").withFormUrlEncodedBody(
            "selfEmploymentStartDate.day"   -> "31",
            "selfEmploymentStartDate.month" -> "2",
            "selfEmploymentStartDate.year"  -> "2000"
          )

        val result = controllerUnderTest.onSubmit(userAnswersId, NormalMode)(request)

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual mockContent
      }

      "must save the date and redirect after a valid submission" in new Setup {

        when(sessionRepository.set(any[UserAnswers])).thenReturn(Future.successful(true))

        val request: FakeRequest[AnyContentAsFormUrlEncoded] =
          FakeRequest(POST, "/").withFormUrlEncodedBody(validDateData.toSeq*)

        private val result = controllerUnderTest.onSubmit(userAnswersId, NormalMode)(request)

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustEqual Some(onwardRoute.url)

        val captor: ArgumentCaptor[UserAnswers] =
          ArgumentCaptor.forClass(classOf[UserAnswers])

        verify(sessionRepository).set(captor.capture())
        captor.getValue.get(SelfEmploymentStartDatePage) mustBe Some(validDate)
      }
    }
  }

  private class Setup(
    userAnswers: UserAnswers = emptyUserAnswers
  ) {
    protected val mockContent                           = "SelfEmploymentStartDateView"
    protected val mockView: SelfEmploymentStartDateView = mock

    when(mockView.apply(any[Form[LocalDate]], any[String], any[Mode])(any[Request[?]], any[Messages]))
      .thenReturn(Html(mockContent))

    protected val sessionRepository: SessionRepository = mock
    private val formProvider                           = new SelfEmploymentStartDateFormProvider()

    protected lazy val controllerUnderTest = new SelfEmploymentStartDateController(
      new MockAppControllerComponents(userAnswers),
      formProvider,
      sessionRepository,
      new Cwf1Navigator with FakeNavigator(onwardRoute),
      mockView
    )
  }
}
