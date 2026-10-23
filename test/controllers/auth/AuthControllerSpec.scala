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

package controllers.auth

import base.SpecBase
import config.FrontendAppConfig
import controllers.{AppControllerComponents, MockAppControllerComponents}
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{clearInvocations, times, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository

import java.net.URLEncoder
import scala.concurrent.Future

class AuthControllerSpec extends SpecBase with GuiceOneAppPerSuite with MockitoSugar with BeforeAndAfterEach {
  import play.api.Application
  import play.api.inject.guice.GuiceApplicationBuilder

  private lazy val appConfig             = app.injector.instanceOf[FrontendAppConfig]
  private lazy val mockSessionRepository = mock[SessionRepository]
  when(mockSessionRepository.clearByUserId(any[String])).thenReturn(Future.successful(true))

  override def fakeApplication(): Application = new GuiceApplicationBuilder()
    .overrides(
      bind[AppControllerComponents] toInstance new MockAppControllerComponents(),
      bind[SessionRepository] toInstance mockSessionRepository
    )
    .build()

  "signOut" - {

    "must clear user answers and redirect to sign out, specifying the exit survey as the continue URL" in {
      val request = FakeRequest(GET, routes.AuthController.signOut().url)

      val result = route(app, request).value

      val encodedContinueUrl  = URLEncoder.encode(appConfig.exitSurveyUrl, "UTF-8")
      val expectedRedirectUrl = s"${appConfig.signOutUrl}?continue=$encodedContinueUrl"

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual expectedRedirectUrl
      verify(mockSessionRepository, times(1)).clearByUserId(eqTo(testUserId))
    }
  }

  "signOutNoSurvey" - {

    "must clear user answers and redirect to sign out, specifying SignedOut as the continue URL" in {
      val request = FakeRequest(GET, routes.AuthController.signOutNoSurvey().url)

      val result = route(app, request).value

      val encodedContinueUrl  = URLEncoder.encode(routes.SignedOutController.onPageLoad().url, "UTF-8")
      val expectedRedirectUrl = s"${appConfig.signOutUrl}?continue=$encodedContinueUrl"

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual expectedRedirectUrl
      verify(mockSessionRepository, times(1)).clearByUserId(eqTo(testUserId))
    }
  }

  override def afterEach(): Unit = clearInvocations(mockSessionRepository)
}
