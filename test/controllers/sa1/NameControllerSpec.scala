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
import controllers.actions.{FakeDataRequiredAction, FakeIdentifierAction, SA1DataRequiredAction}
import forms.sa1.NameFormProvider
import models.sa1.Name
import models.{CheckMode, Mode, NormalMode, UserAnswers}
import org.apache.pekko.stream.Materializer
import org.apache.pekko.stream.testkit.NoMaterializer
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{mock, never, verify, when}
import pages.sa1.NamePage
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.Request
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import play.twirl.api.Html
import repositories.SessionRepository
import views.html.sa1.NameView

import scala.concurrent.Future

class NameControllerSpec extends SpecBase {

  implicit val mat: Materializer = NoMaterializer

  "NameController" - {

    "must return OK and render the view with an empty form for a GET" in new Setup {
      val result = controller.onPageLoad(userAnswersId, NormalMode)(FakeRequest(GET, "/"))

      status(result) mustEqual OK
      contentAsString(result) mustEqual stubContent
      capturedForm().value mustBe None
    }

    "must populate the view with the saved name on a GET when the question has been answered" in new SetupWithSavedName {
      val result = controller.onPageLoad(userAnswersId, NormalMode)(FakeRequest(GET, "/"))

      status(result) mustEqual OK
      capturedForm().value mustBe Some(Name("Jane", "Doe"))
    }

    "must render the view in check mode for a GET in check mode" in new SetupWithSavedName {
      val result = controller.onPageLoad(userAnswersId, CheckMode)(FakeRequest(GET, "/"))

      status(result) mustEqual OK
      verify(stubView).apply(any[Form[Name]], eqTo(userAnswersId), eqTo[Mode](CheckMode))(
        any[Request[?]],
        any[Messages]
      )
    }

    "must redirect to Journey Recovery for a GET when no existing data is found" in new SetupWithoutUserAnswers {
      val result = controller.onPageLoad(userAnswersId, NormalMode)(FakeRequest(GET, "/"))

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
    }

    "must save the name and redirect to the next page when valid data is submitted" in new Setup {
      val request = FakeRequest(POST, "/").withFormUrlEncodedBody(validData.toSeq*)

      val result = controller.onSubmit(userAnswersId, NormalMode)(request)

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual
        controllers.sa1.routes.NameController.onPageLoad(userAnswersId, NormalMode).url

      val savedAnswers: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
      verify(mockSessionRepository).set(savedAnswers.capture())
      savedAnswers.getValue.get(NamePage) mustBe Some(Name("Jane", "Doe"))
    }

    "must save the name and redirect when valid data is submitted in check mode" in new Setup {
      val request = FakeRequest(POST, "/").withFormUrlEncodedBody(validData.toSeq*)

      val result = controller.onSubmit(userAnswersId, CheckMode)(request)

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual
        controllers.sa1.routes.NameController.onPageLoad(userAnswersId, NormalMode).url
      verify(mockSessionRepository).set(any[UserAnswers])
    }

    "must return a Bad Request and show errors when invalid data is submitted" in new Setup {
      val request = FakeRequest(POST, "/").withFormUrlEncodedBody("firstName" -> "", "lastName" -> "Doe")

      val result = controller.onSubmit(userAnswersId, NormalMode)(request)

      status(result) mustEqual BAD_REQUEST
      contentAsString(result) mustEqual stubContent
      capturedForm().errors.map(_.message) mustEqual Seq("sa1.name.firstName.error.required")
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }

    "must redirect to Journey Recovery for a POST when no existing data is found" in new SetupWithoutUserAnswers {
      val request = FakeRequest(POST, "/").withFormUrlEncodedBody(validData.toSeq*)

      val result = controller.onSubmit(userAnswersId, NormalMode)(request)

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }
  }

  trait Setup {
    def userAnswers: Option[UserAnswers] = Some(emptyUserAnswers)

    val mockSessionRepository: SessionRepository = mock(classOf[SessionRepository])
    when(mockSessionRepository.set(any[UserAnswers])).thenReturn(Future.successful(true))

    val stubContent: String = "Name view"
    val stubView: NameView  = mock(classOf[NameView])
    when(stubView.apply(any[Form[Name]], any[String], any[Mode])(any[Request[?]], any[Messages]))
      .thenReturn(Html(stubContent))

    val controller = new NameController(
      stubMessagesApi(),
      mockSessionRepository,
      new FakeIdentifierAction(stubPlayBodyParsers(mat)),
      new SA1DataRequiredAction(new FakeDataRequiredAction(userAnswers)),
      new NameFormProvider(),
      stubMessagesControllerComponents(),
      stubView
    )

    val validData: Map[String, String] = Map("firstName" -> "Jane", "lastName" -> "Doe")

    def capturedForm(): Form[Name] = {
      val formCaptor: ArgumentCaptor[Form[Name]] = ArgumentCaptor.forClass(classOf[Form[Name]])
      verify(stubView).apply(formCaptor.capture(), any[String], any[Mode])(any[Request[?]], any[Messages])
      formCaptor.getValue
    }
  }

  trait SetupWithSavedName extends Setup {
    override def userAnswers: Option[UserAnswers] =
      Some(emptyUserAnswers.set(NamePage, Name("Jane", "Doe")).success.value)
  }

  trait SetupWithoutUserAnswers extends Setup {
    override def userAnswers: Option[UserAnswers] = None
  }
}
