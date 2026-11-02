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

package controllers.actions

import base.ISpecBase
import config.FrontendAppConfig
import controllers.routes
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.{Action, AnyContent, Results}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.auth.core.*

import scala.concurrent.Future

class AuthActionISpec extends ISpecBase with MockitoSugar {
  import org.mockito.ArgumentMatchers.any
  import org.mockito.Mockito.when
  import play.api.Application
  import play.api.inject.bind
  import play.api.inject.guice.GuiceApplicationBuilder

  private val mockAuthConnector = mock[AuthConnector]

  override def fakeApplication(): Application = new GuiceApplicationBuilder()
    .overrides(bind[AuthConnector] toInstance mockAuthConnector)
    .build()

  private lazy val appConfig       = app.injector.instanceOf[FrontendAppConfig]
  private lazy val actionUnderTest = app.injector.instanceOf[IdentifierAction]

  object Harness {
    def onPageLoad(): Action[AnyContent] = actionUnderTest(_ => Results.Ok)
  }

  "Auth Action" - {

    "when the user hasn't logged in" - {

      "must redirect the user to log in " in {
        when(mockAuthConnector.authorise(any, any)(any, any)) thenReturn Future.failed(new MissingBearerToken)

        val result = Harness.onPageLoad()(FakeRequest())

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value must startWith(appConfig.loginUrl)
      }
    }

    "the user's session has expired" - {

      "must redirect the user to log in " in {
        when(mockAuthConnector.authorise(any, any)(any, any)) thenReturn Future.failed(new BearerTokenExpired)

        val result = Harness.onPageLoad()(FakeRequest())

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value must startWith(appConfig.loginUrl)
      }
    }

    "the user doesn't have sufficient enrolments" - {

      "must redirect the user to the unauthorised page" in {
        when(mockAuthConnector.authorise(any, any)(any, any)) thenReturn Future.failed(new InsufficientEnrolments)

        val result = Harness.onPageLoad()(FakeRequest())

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.UnauthorisedController.onPageLoad().url
      }
    }

    "the user doesn't have sufficient confidence level" - {

      "must redirect the user to the iv uplift url" in {
        when(mockAuthConnector.authorise(any, any)(any, any)) thenReturn Future.failed(new InsufficientConfidenceLevel)

        val result = Harness.onPageLoad()(FakeRequest())

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value.contains("mdtp/uplift?origin=shortform&confidenceLevel=250")
      }
    }

    "the user used an unaccepted auth provider" - {

      "must redirect the user to the unauthorised page" in {
        when(mockAuthConnector.authorise(any, any)(any, any)) thenReturn Future.failed(new UnsupportedAuthProvider)

        val result = Harness.onPageLoad()(FakeRequest())

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.UnauthorisedController.onPageLoad().url
      }
    }

    "the user has an unsupported affinity group" - {

      "must redirect the user to the unauthorised page" in {
        when(mockAuthConnector.authorise(any, any)(any, any)) thenReturn Future.failed(new UnsupportedAffinityGroup)

        val result = Harness.onPageLoad()(FakeRequest())

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.UnauthorisedController.onPageLoad().url)
      }
    }

    "the user has an unsupported credential role" - {

      "must redirect the user to the unauthorised page" in {
        when(mockAuthConnector.authorise(any, any)(any, any)) thenReturn Future.failed(new UnsupportedCredentialRole)

        val result = Harness.onPageLoad()(FakeRequest())

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.UnauthorisedController.onPageLoad().url)
      }
    }
  }
}
