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
import forms.cwf1.ContactDetailsFormProvider
import models.cwf1.ContactDetails
import models.{NormalMode, UserAnswers}
import navigation.{Cwf1Navigator, FakeNavigator}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.cwf1.ContactDetailsPage
import play.api.data.{Form, FormError}
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import play.twirl.api.Html
import repositories.SessionRepository
import views.html.cwf1.ContactDetailsView

import scala.concurrent.Future

class ContactDetailsControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  "ContactDetails Controller" - {

    "must return OK and the correct view for a GET" in new Setup {
      private val result = controllerUnderTest.onPageLoad(userAnswersId, NormalMode)(FakeRequest())

      status(result) mustEqual OK
      contentAsString(result) mustEqual mockContent
    }

    "must populate the view correctly on a GET when the question has previously been answered" in new Setup(
      userAnswers = emptyUserAnswers.set(ContactDetailsPage, ContactDetails("value 1", "value 2")).success.value
    ) {
      private val result = controllerUnderTest.onPageLoad(userAnswersId, NormalMode)(FakeRequest())

      status(result) mustEqual OK
      contentAsString(result) mustEqual mockContent

      val formCaptor = ArgumentCaptor.forClass(classOf[Form[ContactDetails]])

      verify(mockView).apply(
        formCaptor.capture(),
        eqTo(userAnswersId),
        eqTo(NormalMode)
      )(any(), any())

      formCaptor.getValue.value mustBe Some(
        ContactDetails("value 1", "value 2")
      )
    }

    "must redirect to the next page when valid data is submitted" in new Setup {
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val request =
        FakeRequest()
          .withFormUrlEncodedBody(("emailAddress", "name@example.com"), ("phoneNumber", "0746689076"))

      private val result = controllerUnderTest.onSubmit(userAnswersId, NormalMode)(request)

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual onwardRoute.url

      verify(mockSessionRepository).set(any())
    }

    "must return a Bad Request and errors when invalid data is submitted" in new Setup {
      val request =
        FakeRequest()
          .withFormUrlEncodedBody(("value", "invalid value"))

      val boundForm = form.bind(Map("value" -> "invalid value"))

      private val result = controllerUnderTest.onSubmit(userAnswersId, NormalMode)(request)

      status(result) mustEqual BAD_REQUEST
      contentAsString(result) mustEqual mockContent

      val formCaptor = ArgumentCaptor.forClass(classOf[Form[ContactDetails]])

      verify(mockView).apply(
        formCaptor.capture(),
        eqTo(userAnswersId),
        eqTo(NormalMode)
      )(any(), any())

      formCaptor.getValue.errors must contain(
        FormError(
          "emailAddress",
          "cwf1.contactDetails.error.emailAddress.required"
        )
      )
    }
  }

  private class Setup(
    userAnswers: UserAnswers = emptyUserAnswers
  ) {
    protected val mockView: ContactDetailsView = mock[ContactDetailsView]
    protected val mockContent                  = "ContactDetailsView"

    when(mockView(any(), any(), any())(any(), any()))
      .thenReturn(Html(mockContent))

    private val formProvider = new ContactDetailsFormProvider()
    protected val form       = formProvider()

    protected val mockSessionRepository: SessionRepository = mock[SessionRepository]

    protected lazy val controllerUnderTest = new ContactDetailsController(
      new MockAppControllerComponents(userAnswers),
      mockSessionRepository,
      formProvider,
      new Cwf1Navigator with FakeNavigator(onwardRoute),
      mockView
    )
  }
}
