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
import forms.sa1.WhyTaxReturnFormProvider
import models.sa1.WhyTaxReturn
import models.{NormalMode, UserAnswers}
import navigation.Navigator
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.sa1.WhyTaxReturnPage
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import play.twirl.api.Html
import repositories.SessionRepository
import views.html.sa1.WhyTaxReturnView

import scala.concurrent.Future

class WhyTaxReturnControllerSpec extends SpecBase with MockitoSugar {

  private val onwardRoute = Call("GET", "/foo")

  private lazy val whyTaxReturnRoute =
    controllers.sa1.routes.WhyTaxReturnController.onPageLoad(userAnswersId, NormalMode).url

  "WhyTaxReturn Controller" - {

    "must redirect to the next page when valid data is submitted" in new Setup {
      when(mockSessionRepository.set(any[UserAnswers])) thenReturn Future.successful(true)
      when(mockNavigator.nextPage(any(), any(), any())) thenReturn onwardRoute

      private val request = FakeRequest(POST, whyTaxReturnRoute)
        .withFormUrlEncodedBody(
          "value[0]" -> WhyTaxReturn.CompanyDirector.toString,
          "value[1]" -> WhyTaxReturn.Other.toString
        )

      private val result = controller.onSubmit(userAnswersId, NormalMode)(request)

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual onwardRoute.url

      private val expectedAnswers = userAnswers.set(WhyTaxReturnPage, answer).success.value
      verify(mockSessionRepository).set(eqTo(expectedAnswers))
      verify(mockNavigator).nextPage(eqTo(WhyTaxReturnPage), eqTo(NormalMode), eqTo(expectedAnswers))
    }

    "must return a Bad Request and errors when no reason is selected" in new Setup {
      private val result    = controller.onSubmit(userAnswersId, NormalMode)(FakeRequest(POST, whyTaxReturnRoute))
      private val boundForm = form.bind(Map.empty[String, String])

      status(result) mustEqual BAD_REQUEST
      contentAsString(result) mustEqual mockContent

      verify(mockView).apply(eqTo(boundForm), eqTo(userAnswersId), eqTo(NormalMode))(any(), any())
    }

    "must return a Bad Request and errors when an invalid reason is submitted" in new Setup {
      private val request = FakeRequest(POST, whyTaxReturnRoute)
        .withFormUrlEncodedBody("value[0]" -> "invalid value")

      private val result    = controller.onSubmit(userAnswersId, NormalMode)(request)
      private val boundForm = form.bind(Map("value[0]" -> "invalid value"))

      status(result) mustEqual BAD_REQUEST
      contentAsString(result) mustEqual mockContent

      verify(mockView).apply(eqTo(boundForm), eqTo(userAnswersId), eqTo(NormalMode))(any(), any())
    }
  }

  private class Setup {
    protected val userAnswers: UserAnswers = emptyUserAnswers
    protected val answer: Set[WhyTaxReturn] = Set(WhyTaxReturn.CompanyDirector, WhyTaxReturn.Other)

    protected val mockSessionRepository: SessionRepository = mock
    protected val mockNavigator: Navigator                 = mock
    protected val mockView: WhyTaxReturnView               = mock
    protected val mockContent                              = "WhyTaxReturnView"

    private val formProvider = new WhyTaxReturnFormProvider()
    protected val form       = formProvider()

    when(mockView(any(), any(), any())(any(), any())) thenReturn Html(mockContent)

    protected lazy val controller = new WhyTaxReturnController(
      new MockAppControllerComponents(userAnswers),
      mockSessionRepository,
      mockNavigator,
      formProvider,
      mockView
    )
  }
}
