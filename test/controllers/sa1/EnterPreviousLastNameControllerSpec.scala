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

import controllers.actions.{FakeDataRequiredAction, FakeIdentifierAction, SA1DataRequiredAction}
import forms.sa1.PreviousNameFormProvider
import models.{JourneyType, NormalMode, UserAnswers}
import models.sa1.PreviousName
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{verify, when}
import org.scalatest.TryValues
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import pages.sa1.PreviousLastNamePage
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.{AnyContentAsFormUrlEncoded, Request}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import play.twirl.api.Html
import repositories.SessionRepository
import views.html.sa1.EnterPreviousLastNameView

import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}

class EnterPreviousLastNameControllerSpec extends AnyWordSpec with Matchers with TryValues with MockitoSugar {
  private implicit val ec: ExecutionContext = ExecutionContext.global

  private val journeyId       = "test-journey-id"
  private val userId          = "test-user-id"
  private val previousName    = PreviousName(previousLastName = "John Doe", lastNameChangeDate = LocalDate.of(2020, 5, 10))
  private val mockViewContent = "test view"
  private val validFormData   = Map(
    "previousLastName"         -> "John Doe",
    "lastNameChangeDate.day"   -> "10",
    "lastNameChangeDate.month" -> "5",
    "lastNameChangeDate.year"  -> "2020"
  )

  private class Setup {
    protected def userAnswers: UserAnswers             = UserAnswers(journeyId, userId, JourneyType.SA1)
    protected val sessionRepository: SessionRepository = mock[SessionRepository]
    protected val view: EnterPreviousLastNameView      = mock[EnterPreviousLastNameView]

    private val controllerComponents    = stubMessagesControllerComponents()
    private val identifierAction        = new FakeIdentifierAction(controllerComponents.parsers)
    private lazy val dataRequiredAction = new SA1DataRequiredAction(new FakeDataRequiredAction(Some(userAnswers)))
    private val formProvider            = new PreviousNameFormProvider()

    protected lazy val controller = new EnterPreviousLastNameController(
      controllerComponents,
      identifierAction,
      dataRequiredAction,
      formProvider,
      sessionRepository,
      view
    )

    when(view.apply(any[Form[PreviousName]], any[String])(any[Request[?]], any[Messages]))
      .thenReturn(Html(mockViewContent))
  }

  "EnterPreviousLastNameController" should {

    "return OK when loading the page" in new Setup {
      val result = controller.onPageLoad(journeyId, NormalMode)(FakeRequest())

      status(result) mustBe OK
      contentAsString(result) mustBe mockViewContent
    }

    "pre-populate the form when previous name data exists" in new Setup {
      override protected val userAnswers: UserAnswers =
        UserAnswers(journeyId, userId, JourneyType.SA1).set(PreviousLastNamePage, previousName).success.value

      val result = controller.onPageLoad(journeyId, NormalMode)(FakeRequest())

      status(result) mustBe OK
      val captor: ArgumentCaptor[Form[PreviousName]] = ArgumentCaptor.forClass(classOf[Form[PreviousName]])
      verify(view).apply(captor.capture(), eqTo(journeyId))(any[Request[?]], any[Messages])

      val populatedForm: Form[PreviousName] = captor.getValue
      populatedForm("previousLastName").value mustBe Some("John Doe")
      populatedForm("lastNameChangeDate.day").value mustBe Some("10")
      populatedForm("lastNameChangeDate.month").value mustBe Some("5")
      populatedForm("lastNameChangeDate.year").value mustBe Some("2020")
    }

    "return BAD_REQUEST when the submitted form is invalid" in new Setup {
      val request: FakeRequest[AnyContentAsFormUrlEncoded] = FakeRequest(POST, "/").withFormUrlEncodedBody(
        "previousLastName"         -> "",
        "lastNameChangeDate.day"   -> "10",
        "lastNameChangeDate.month" -> "5",
        "lastNameChangeDate.year"  -> "2020"
      )

      val result = controller.onSubmit(journeyId, NormalMode)(request)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe mockViewContent
    }

    "save the submitted data and redirect when the form is valid" in new Setup {
      when(sessionRepository.set(any[UserAnswers])).thenReturn(Future.successful(true))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] =
        FakeRequest(POST, "/").withFormUrlEncodedBody(validFormData.toSeq*)

      val result = controller.onSubmit(journeyId, NormalMode)(request)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(routes.DateOfBirthController.onPageLoad(journeyId, NormalMode).url)

      val captor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
      verify(sessionRepository).set(captor.capture())
      captor.getValue.get(PreviousLastNamePage) mustBe Some(previousName)
    }
  }
}
