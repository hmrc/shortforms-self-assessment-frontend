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
import forms.cwf1.IsBusinessAddressSameAsHomeAddressFormProvider
import models.{NormalMode, UserAnswers}
import navigation.{Cwf1Navigator, FakeNavigator}
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.cwf1.IsBusinessAddressSameAsHomeAddressPage
import play.api.data.Form
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import play.twirl.api.Html
import repositories.SessionRepository
import views.html.cwf1.IsBusinessAddressSameAsHomeAddressView

import scala.concurrent.Future

class IsBusinessAddressSameAsHomeAddressControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  "IsBusinessAddressSameAsHomeAddress Controller" - {

    "must populate the view correctly on a GET when the question has previously been answered" in new Setup {
      val userAnswers: UserAnswers = emptyUserAnswers.set(IsBusinessAddressSameAsHomeAddressPage, true).success.value

      private val result = controllerUnderTest.onPageLoad(userAnswersId, NormalMode)(FakeRequest())

      status(result) mustEqual OK
      contentAsString(result) mustEqual mockContent

      verify(mockView).apply(
        eqTo(form.fill(true)),
        eqTo(userAnswersId),
        eqTo(NormalMode)
      )(any(), any())
    }

    "must redirect to the next page when valid data is submitted" in new Setup {
      val userAnswers: UserAnswers = emptyUserAnswers
      private val request          = FakeRequest().withFormUrlEncodedBody(("value", "true"))

      when(mockSessionRepository.set(any[UserAnswers]))
        .thenReturn(Future.successful(true))

      private val result = controllerUnderTest.onSubmit(userAnswersId, NormalMode)(request)

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual onwardRoute.url

      verify(mockSessionRepository).set(any())
    }

  }

  abstract private class Setup {
    protected val userAnswers: UserAnswers

    protected val mockSessionRepository: SessionRepository         = mock[SessionRepository]
    protected val mockView: IsBusinessAddressSameAsHomeAddressView = mock
    protected val mockContent                                      = "IsBusinessAddressSameAsHomeAddress"
    private val formProvider                                       = new IsBusinessAddressSameAsHomeAddressFormProvider()
    protected val form: Form[Boolean]                              = formProvider()

    when(mockView(any(), any(), any())(any(), any()))
      .thenReturn(Html(mockContent))

    protected lazy val controllerUnderTest = new IsBusinessAddressSameAsHomeAddressController(
      mockSessionRepository,
      new Cwf1Navigator with FakeNavigator(onwardRoute),
      formProvider,
      new MockAppControllerComponents(userAnswers),
      mockView
    )
  }
}
