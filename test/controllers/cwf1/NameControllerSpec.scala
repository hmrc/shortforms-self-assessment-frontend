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
import forms.cwf1.NameFormProvider
import models.cwf1.Name
import models.{CheckMode, Mode, NormalMode, UserAnswers}
import navigation.Navigator
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{mock, never, verify, when}
import pages.cwf1.NamePage
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.Request
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import play.twirl.api.Html
import repositories.SessionRepository
import views.html.cwf1.NameView

import scala.concurrent.Future

class NameControllerSpec extends SpecBase {

  "NameController" - {

    "must return OK and render the view with an empty form for a GET" in new Setup {
      private val result = controllerUnderTest.onPageLoad(userAnswersId, NormalMode)(FakeRequest())

      status(result) mustEqual OK
      contentAsString(result) mustEqual stubContent
      capturedForm().value mustBe None
    }

    "must populate the view with the saved name on a GET when the question has been answered" in new Setup {
      override val givenUserAnswers: UserAnswers =
        emptyUserAnswers.set(NamePage, Name("Jane", "Doe")).success.value

      private val result = controllerUnderTest.onPageLoad(userAnswersId, NormalMode)(FakeRequest())

      status(result) mustEqual OK
      capturedForm().value mustBe Some(Name("Jane", "Doe"))
    }

    "must render the view in check mode for a GET in check mode" in new Setup {
      override val givenUserAnswers: UserAnswers =
        emptyUserAnswers.set(NamePage, Name("Jane", "Doe")).success.value

      private val result = controllerUnderTest.onPageLoad(userAnswersId, CheckMode)(FakeRequest())

      status(result) mustEqual OK
      verify(stubView).apply(any[Form[Name]], eqTo(userAnswersId), eqTo[Mode](CheckMode))(
        any[Request[?]],
        any[Messages]
      )
    }

    "must save the name and redirect to the next page when valid data is submitted" in new Setup {
      emptyUserAnswers.set(NamePage, Name("Jane", "Doe")).success.value

      private val request = FakeRequest().withFormUrlEncodedBody(validData.toSeq*)

      private val result = controllerUnderTest.onSubmit(userAnswersId, NormalMode)(request)

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.cwf1.routes.NameController.onPageLoad(journeyId = "test-journey-id", NormalMode).url //TODO

    }

    "must return a Bad Request and show errors when invalid data is submitted" in new Setup {
      private val request = FakeRequest().withFormUrlEncodedBody("firstName" -> "", "lastName" -> "Doe")

      private val result = controllerUnderTest.onSubmit(userAnswersId, NormalMode)(request)

      status(result) mustEqual BAD_REQUEST
      contentAsString(result) mustEqual stubContent
      capturedForm().errors.map(_.message) mustEqual Seq("cwf1.name.firstName.error.required")
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }
  }

  trait Setup {
    protected val givenUserAnswers: UserAnswers  = emptyUserAnswers
    protected val navigator: Navigator           = mock(classOf[Navigator])
    val mockSessionRepository: SessionRepository = mock(classOf[SessionRepository])
    when(mockSessionRepository.set(any[UserAnswers])).thenReturn(Future.successful(true))

    val stubContent: String = "Name view"
    val stubView: NameView  = mock(classOf[NameView])
    when(stubView.apply(any[Form[Name]], any[String], any[Mode])(any[Request[?]], any[Messages]))
      .thenReturn(Html(stubContent))

    protected lazy val controllerUnderTest = new NameController(
      mockSessionRepository,
      new NameFormProvider(),
      new MockAppControllerComponents(givenUserAnswers),
      navigator,
      stubView
    )

    val validData: Map[String, String] = Map("firstName" -> "Jane", "lastName" -> "Doe")

    def capturedForm(): Form[Name] = {
      val formCaptor: ArgumentCaptor[Form[Name]] = ArgumentCaptor.forClass(classOf[Form[Name]])
      verify(stubView).apply(formCaptor.capture(), any[String], any[Mode])(any[Request[?]], any[Messages])
      formCaptor.getValue
    }
  }
}
