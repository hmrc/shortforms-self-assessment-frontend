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
import forms.cwf1.DateOfBirthFormProvider
import models.{Mode, NormalMode, UserAnswers}
import navigation.{Cwf1Navigator, FakeNavigator}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.cwf1.DateOfBirthPage
import play.api.data.Form
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import play.twirl.api.Html
import repositories.SessionRepository
import views.html.cwf1.DateOfBirthView

import java.time.LocalDate
import scala.concurrent.Future

class DateOfBirthControllerSpec extends SpecBase with MockitoSugar {
  private val onwardRoute = Call(GET, "/next-page")

  private val validDate = LocalDate.of(1990, 5, 10)

  private val validDateData = Map(
    "dateOfBirth.day"   -> "10",
    "dateOfBirth.month" -> "5",
    "dateOfBirth.year"  -> "1990"
  )

  "DateOfBirthController" - {

    "onPageLoad" - {

      "must return OK when loading the page without a saved date" in new Setup {
        private val result = controllerUnderTest.onPageLoad(userAnswersId, NormalMode)(FakeRequest())

        status(result) mustEqual OK
        contentAsString(result) mustEqual mockContent
      }

      "must pre-populate the form when a date has already been saved" in new Setup(
        emptyUserAnswers.set(DateOfBirthPage, validDate).success.value
      ) {
        private val result = controllerUnderTest.onPageLoad(userAnswersId, NormalMode)(FakeRequest())

        status(result) mustEqual OK

        private val captor = ArgumentCaptor.forClass(classOf[Form[LocalDate]])
        verify(mockView).apply(captor.capture(), eqTo(userAnswersId), eqTo[Mode](NormalMode))(any, any)

        private val populatedForm = captor.getValue

        populatedForm("dateOfBirth.day").value mustBe Some("10")
        populatedForm("dateOfBirth.month").value mustBe Some("5")
        populatedForm("dateOfBirth.year").value mustBe Some("1990")
      }
    }

    "onSubmit" - {

      "must return BAD_REQUEST when the submitted date is invalid" in new Setup {
        private val request =
          FakeRequest().withFormUrlEncodedBody(
            "dateOfBirth.day"   -> "31",
            "dateOfBirth.month" -> "2",
            "dateOfBirth.year"  -> "2000"
          )

        private val result = controllerUnderTest.onSubmit(userAnswersId, NormalMode)(request)

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual mockContent
      }

      "must save the date and redirect after a valid submission" in new Setup {
        when(sessionRepository.set(any[UserAnswers])) thenReturn Future.successful(true)

        private val request = FakeRequest().withFormUrlEncodedBody(validDateData.toSeq*)

        private val result = controllerUnderTest.onSubmit(userAnswersId, NormalMode)(request)

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url

        private val captor = ArgumentCaptor.forClass(classOf[UserAnswers])

        verify(sessionRepository).set(captor.capture())
        captor.getValue.get(DateOfBirthPage) mustBe Some(validDate)
      }
    }
  }

  private class Setup(
    userAnswers: UserAnswers = emptyUserAnswers
  ) {
    protected val mockContent               = "DateOfBirthView"
    protected val mockView: DateOfBirthView = mock

    when(mockView.apply(any[Form[LocalDate]], any[String], any[Mode])(any, any)) thenReturn Html(mockContent)

    protected val sessionRepository: SessionRepository = mock
    private val formProvider                           = new DateOfBirthFormProvider()

    protected lazy val controllerUnderTest = new DateOfBirthController(
      new MockAppControllerComponents(userAnswers),
      formProvider,
      sessionRepository,
      new Cwf1Navigator with FakeNavigator(onwardRoute),
      mockView
    )
  }
}
