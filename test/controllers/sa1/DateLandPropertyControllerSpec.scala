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
import forms.sa1.DateLandPropertyFormProvider
import models.{CheckMode, JourneyType, NormalMode, UserAnswers}
import navigation.Navigator
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{verify, when}
import org.scalatest.TryValues
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import pages.sa1.DateLandPropertyPage
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.{AnyContentAsFormUrlEncoded, Request}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import play.twirl.api.Html
import repositories.SessionRepository
import views.html.sa1.DateLandPropertyView

import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}

class DateLandPropertyControllerSpec extends AnyWordSpec with Matchers with TryValues with MockitoSugar {
  private implicit val ec: ExecutionContext = ExecutionContext.global

  private val journeyId       = "test-journey-id"
  private val userId          = "test-user-id"
  private val date            = LocalDate.of(2022, 3, 19)
  private val mockViewContent = "test view"
  private val validFormData   = Map(
    "dateLandPropertyUk.day"   -> "19",
    "dateLandPropertyUk.month" -> "3",
    "dateLandPropertyUk.year"  -> "2022"
  )

  private class Setup {
    protected def userAnswers: UserAnswers             = UserAnswers(journeyId, userId, JourneyType.SA1)
    protected val sessionRepository: SessionRepository = mock[SessionRepository]
    protected val view: DateLandPropertyView           = mock[DateLandPropertyView]

    private val controllerComponents    = stubMessagesControllerComponents()
    private val identifierAction        = new FakeIdentifierAction(controllerComponents.parsers)
    private lazy val dataRequiredAction = new SA1DataRequiredAction(new FakeDataRequiredAction(Some(userAnswers)))
    private val formProvider            = new DateLandPropertyFormProvider()
    private val navigator               = new Navigator()

    protected lazy val controller = new DateLandPropertyController(
      controllerComponents,
      identifierAction,
      dataRequiredAction,
      formProvider,
      sessionRepository,
      navigator,
      view
    )

    when(view.apply(any[Form[LocalDate]], any[String], any())(any[Request[?]], any[Messages]))
      .thenReturn(Html(mockViewContent))
  }

  "DateLandPropertyController" should {

    "return OK when loading the page" in new Setup {
      val result = controller.onPageLoad(journeyId, NormalMode)(FakeRequest())

      status(result) mustBe OK
      contentAsString(result) mustBe mockViewContent
    }

    "pre-populate the form when a date already exists" in new Setup {
      override protected def userAnswers: UserAnswers =
        UserAnswers(journeyId, userId, JourneyType.SA1).set(DateLandPropertyPage, date).success.value

      val result = controller.onPageLoad(journeyId, NormalMode)(FakeRequest())

      status(result) mustBe OK
      val captor: ArgumentCaptor[Form[LocalDate]] = ArgumentCaptor.forClass(classOf[Form[LocalDate]])
      verify(view).apply(captor.capture(), eqTo(journeyId), eqTo(NormalMode))(any[Request[?]], any[Messages])

      val populatedForm = captor.getValue
      populatedForm("dateLandPropertyUk.day").value mustBe Some("19")
      populatedForm("dateLandPropertyUk.month").value mustBe Some("3")
      populatedForm("dateLandPropertyUk.year").value mustBe Some("2022")
    }

    "return BAD_REQUEST when the submitted date is invalid" in new Setup {
      val request: FakeRequest[AnyContentAsFormUrlEncoded] = FakeRequest(POST, "/").withFormUrlEncodedBody(
        "dateLandPropertyUk.day"   -> "",
        "dateLandPropertyUk.month" -> "3",
        "dateLandPropertyUk.year"  -> "2022"
      )

      val result = controller.onSubmit(journeyId, NormalMode)(request)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe mockViewContent
    }

    "redirect to check your answers in check mode when the form is valid" in new Setup {
      when(sessionRepository.set(any[UserAnswers])).thenReturn(Future.successful(true))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] =
        FakeRequest(POST, "/").withFormUrlEncodedBody(validFormData.toSeq*)

      val result = controller.onSubmit(journeyId, CheckMode)(request)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.CheckYourAnswersController.onPageLoad(journeyId).url)
    }
  }
}
