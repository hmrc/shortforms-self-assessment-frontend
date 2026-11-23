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
import forms.cwf1.BusinessNameFormProvider
import models.{NormalMode, UserAnswers}
import navigation.{Cwf1Navigator, FakeNavigator}
import org.scalatest.LoneElement
import org.scalatestplus.mockito.MockitoSugar
import pages.cwf1.BusinessNamePage
import play.api.data.Form
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import play.twirl.api.Html
import repositories.SessionRepository
import views.html.cwf1.BusinessNameView

import scala.concurrent.Future

class BusinessNameControllerSpec extends SpecBase with MockitoSugar with LoneElement {
  import org.mockito.ArgumentCaptor
  import org.mockito.ArgumentMatchers.{any, eq as eqTo}
  import org.mockito.Mockito.{verify, when}

  private val onwardRoute = Call(GET, "/next-page")

  "page load handler must" - {

    "return OK and the correct view for a GET" in new Setup {
      private val result = controllerUnderTest.onPageLoad(userAnswersId, NormalMode)(FakeRequest())

      status(result) mustEqual OK
      contentAsString(result) mustEqual mockContent
    }

    "populate the view correctly on a GET when the question has previously been answered" in new Setup {
      override val userAnswers: UserAnswers = emptyUserAnswers.update(BusinessNamePage, "ACME Inc")

      private val result = controllerUnderTest.onPageLoad(userAnswersId, NormalMode)(FakeRequest())

      status(result) mustEqual OK
      contentAsString(result) mustEqual mockContent

      private val formCaptor = ArgumentCaptor.forClass(classOf[Form[String]])

      verify(mockView).apply(
        formCaptor.capture(),
        eqTo(userAnswersId),
        eqTo(NormalMode)
      )(any, any)

      formCaptor.getValue.value mustBe Some("ACME Inc")
    }
  }

  "submission handler" - {
    "must redirect to the next page when valid data is submitted" in new Setup {
      when(mockSessionRepository.set(any)) thenReturn Future.successful(true)

      private val request = FakeRequest().withFormUrlEncodedBody("value" -> "ACME Limited")

      private val result = controllerUnderTest.onSubmit(userAnswersId, NormalMode)(request)

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual onwardRoute.url

      verify(mockSessionRepository).set(any)
    }

    "must return a Bad Request and errors when invalid data is submitted" in new Setup {
      private val request = FakeRequest().withFormUrlEncodedBody("value" -> "Some !@£ nonsense %^& with &*( symbols")

      private val result = controllerUnderTest.onSubmit(userAnswersId, NormalMode)(request)

      status(result) mustEqual BAD_REQUEST
      contentAsString(result) mustEqual mockContent

      private val formCaptor = ArgumentCaptor.forClass(classOf[Form[String]])

      verify(mockView).apply(
        formCaptor.capture(),
        eqTo(userAnswersId),
        eqTo(NormalMode)
      )(any(), any())

      formCaptor.getValue.errors.loneElement.message mustBe "businessName.error.invalid"
    }
  }

  private abstract class Setup {
    val userAnswers: UserAnswers = emptyUserAnswers

    protected val mockView: BusinessNameView = mock
    protected val mockContent                = "BusinessName"
    when(mockView(any, any, any)(any, any)) thenReturn Html(mockContent)

    private val formProvider         = new BusinessNameFormProvider()
    protected val form: Form[String] = formProvider()

    protected val mockSessionRepository: SessionRepository = mock

    lazy val controllerUnderTest = new BusinessNameController(
      new MockAppControllerComponents(userAnswers),
      mockSessionRepository,
      formProvider,
      mockView,
      new Cwf1Navigator with FakeNavigator(onwardRoute)
    )
  }
}
