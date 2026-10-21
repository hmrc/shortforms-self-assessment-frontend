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
import forms.sa1.DoYouHaveANinoFormProvider
import models.{NormalMode, UserAnswers}
import navigation.Navigator
import org.apache.pekko.actor.ActorSystem
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.sa1.DoYouHaveANinoPage
import play.api.mvc.{Call, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import play.twirl.api.Html
import repositories.SessionRepository
import views.html.sa1.DoYouHaveANinoView

import scala.concurrent.Future

class DoYouHaveANinoControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  lazy val doYouHaveANinoRoute =
    controllers.sa1.routes.DoYouHaveANinoController.onPageLoad(userAnswersId, NormalMode).url

  "DoYouHaveANino Controller" - {

    "must return OK and the correct view for a GET" in new Setup {
      val userAnswers: UserAnswers = emptyUserAnswers
      val request                  = FakeRequest(GET, doYouHaveANinoRoute)

      val result: Future[Result] = controller.onPageLoad(userAnswersId, NormalMode)(request)

      status(result) mustEqual OK
      contentAsString(result) mustEqual mockContent

      verify(mockView).apply(
        eqTo(form),
        eqTo(userAnswersId),
        eqTo(NormalMode)
      )(any(), any())
    }

    "must populate the view correctly on a GET when the question has previously been answered" in new Setup {
      val userAnswers: UserAnswers = emptyUserAnswers.set(DoYouHaveANinoPage, true).success.value
      val request                  = FakeRequest(GET, doYouHaveANinoRoute)

      val result: Future[Result] = controller.onPageLoad(userAnswersId, NormalMode)(request)

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
      val request                  = FakeRequest(POST, doYouHaveANinoRoute)
        .withFormUrlEncodedBody(("value", "true"))

      when(mockSessionRepository.set(any[UserAnswers]))
        .thenReturn(Future.successful(true))

      when(mockNavigator.nextPage(any(), any(), any())) thenReturn onwardRoute

      val result = controller.onSubmit(userAnswersId, NormalMode)(request)

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual onwardRoute.url

      verify(mockSessionRepository).set(any())
    }

    "must return a Bad Request and errors when invalid data is submitted" in new Setup {
      val userAnswers: UserAnswers = emptyUserAnswers
      val request                  = FakeRequest(POST, doYouHaveANinoRoute)
        .withFormUrlEncodedBody(("value", ""))
      val boundForm                = form.bind(Map("value" -> ""))

      val result = controller.onSubmit(userAnswersId, NormalMode)(request)

      status(result) mustEqual BAD_REQUEST
      contentAsString(result) mustEqual mockContent

      verify(mockView).apply(
        eqTo(boundForm),
        eqTo(userAnswersId),
        eqTo(NormalMode)
      )(any(), any())
    }
  }

  abstract private class Setup {
    implicit val actorSystem: ActorSystem                  = ActorSystem("test")
    protected val userAnswers: UserAnswers
    private val bodyParsers                                = stubPlayBodyParsers()
    private lazy val identifierAction                      =
      new FakeIdentifierAction(bodyParsers)
    private lazy val dataRequiredAction                    =
      new SA1DataRequiredAction(
        new FakeDataRequiredAction(Some(userAnswers))
      )
    protected val mockSessionRepository: SessionRepository = mock[SessionRepository]
    protected val mockNavigator: Navigator                 = mock[Navigator]
    protected val mockView: DoYouHaveANinoView             = mock[DoYouHaveANinoView]
    protected val mockContent                              = "DoYouHaveANino"
    private val doYouHaveANinoFormProvider                 = new DoYouHaveANinoFormProvider()
    protected val form                                     = doYouHaveANinoFormProvider()

    when(mockView(any(), any(), any())(any(), any()))
      .thenReturn(Html(mockContent))

    protected lazy val controller: DoYouHaveANinoController = new DoYouHaveANinoController(
      mockSessionRepository,
      mockNavigator,
      identifierAction,
      dataRequiredAction,
      doYouHaveANinoFormProvider,
      stubMessagesControllerComponents(),
      mockView
    )
  }
}
