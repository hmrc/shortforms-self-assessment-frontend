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
import forms.sa1.NoNinoReasonFormProvider
import models.{Mode, NormalMode, UserAnswers}
import navigation.FakeNavigator
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.sa1.{DoYouHaveANinoPage, NoNinoReasonPage}
import play.api.data.Form
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import play.twirl.api.Html
import repositories.SessionRepository
import views.html.sa1.NoNinoReasonView

import scala.concurrent.Future

class NoNinoReasonControllerSpec extends SpecBase with MockitoSugar {

  private val noNinoReason = "No Nino"

  private val ninoYesOrNoRoute = routes.DoYouHaveANinoController.onPageLoad(userAnswersId, NormalMode)
  private val fakeOnwardRoute  = Call(GET, "/next-page")

  "NoNinoReasonController" - {

    "onPageLoad must" - {

      "return OK and display the page" in new Setup {
        private val result = controllerUnderTest.onPageLoad(userAnswersId, NormalMode)(FakeRequest())

        status(result) mustEqual OK
        contentAsString(result) mustEqual mockContent
      }

      "pre-populate the form when a reason already exists" in new Setup {
        override val userAnswers: UserAnswers = super.userAnswers.update(NoNinoReasonPage, noNinoReason)

        private val result = controllerUnderTest.onPageLoad(userAnswersId, NormalMode)(FakeRequest())
        status(result) mustEqual OK

        private val captor = ArgumentCaptor.forClass(classOf[Form[String]])
        verify(mockView)
          .apply(captor.capture(), eqTo(userAnswersId), eqTo[Mode](NormalMode))(any, any)

        private val populatedForm = captor.getValue
        populatedForm("value").value mustBe Some(noNinoReason)
      }

      "redirect to 'Do You Have a Nino' page WHEN" - {
        "it was answered 'yes'" in new Setup {
          override val userAnswers: UserAnswers = emptyUserAnswers.update(DoYouHaveANinoPage, true)

          private val result = controllerUnderTest.onPageLoad(userAnswersId, NormalMode)(FakeRequest())
          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustBe ninoYesOrNoRoute.url
        }

        "it was NOT answered at all" in new Setup {
          override val userAnswers: UserAnswers = emptyUserAnswers

          private val result = controllerUnderTest.onPageLoad(userAnswersId, NormalMode)(FakeRequest())
          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustBe ninoYesOrNoRoute.url
        }
      }
    }

    "onSubmit" - {

      "redirect to 'Do You Have a Nino' page WHEN" - {
        "it was answered 'yes'" in new Setup {
          override val userAnswers: UserAnswers = emptyUserAnswers.update(DoYouHaveANinoPage, true)

          private val result = controllerUnderTest.onSubmit(userAnswersId, NormalMode)(FakeRequest())
          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustBe ninoYesOrNoRoute.url
        }

        "it was NOT answered at all" in new Setup {
          override val userAnswers: UserAnswers = emptyUserAnswers

          private val result = controllerUnderTest.onSubmit(userAnswersId, NormalMode)(FakeRequest())
          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustBe ninoYesOrNoRoute.url
        }
      }

      "must return BAD_REQUEST when the submitted form is invalid" in new Setup {
        private val request = FakeRequest(POST, "/").withFormUrlEncodedBody("value" -> "")

        private val result = controllerUnderTest.onSubmit(userAnswersId, NormalMode)(request)

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual mockContent
      }

      "must save the submitted data and redirect when the form is valid" in new Setup {
        when(sessionRepository.set(any[UserAnswers])) thenReturn Future.successful(true)

        private val request = FakeRequest(POST, "/").withFormUrlEncodedBody("value" -> noNinoReason)
        private val result  = controllerUnderTest.onSubmit(userAnswersId, NormalMode)(request)

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual fakeOnwardRoute.url

        private val captor = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(sessionRepository).set(captor.capture())
        captor.getValue.get(NoNinoReasonPage) mustBe Some(noNinoReason)
      }
    }
  }

  private class Setup {
    protected def userAnswers: UserAnswers = emptyUserAnswers.update(DoYouHaveANinoPage, false)

    protected val mockContent                = "NoNinoReasonView"
    protected val mockView: NoNinoReasonView = mock
    when(mockView.apply(any[Form[String]], any[String], any[Mode])(any, any)) thenReturn Html(mockContent)

    protected val sessionRepository: SessionRepository = mock

    protected lazy val controllerUnderTest = new NoNinoReasonController(
      new MockAppControllerComponents(userAnswers),
      new NoNinoReasonFormProvider(),
      sessionRepository,
      new FakeNavigator(fakeOnwardRoute),
      mockView
    )
  }
}
