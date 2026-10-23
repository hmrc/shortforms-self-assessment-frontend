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

import base.SpecBase
import config.FrontendAppConfig
import controllers.routes
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.mvc.*
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.auth.core.*

import scala.concurrent.Future

class AuthActionSpec extends SpecBase with GuiceOneAppPerSuite with MockitoSugar with Results {
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

  "Auth Action" - {

    "when the user hasn't logged in" - {

      "must redirect the user to log in " in {

        whenAuthConnectorReturns(Future.failed(new MissingBearerToken))
        val result = invokeHarness()

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value must startWith(appConfig.loginUrl)
      }
    }

    "the user's session has expired" - {

      "must redirect the user to log in " in {

        whenAuthConnectorReturns(Future.failed(new BearerTokenExpired))
        val result = invokeHarness()

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value must startWith(appConfig.loginUrl)
      }
    }

    "the user doesn't have sufficient enrolments" - {

      "must redirect the user to the unauthorised page" in {

        whenAuthConnectorReturns(Future.failed(new InsufficientEnrolments))
        val result = invokeHarness()

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.UnauthorisedController.onPageLoad().url
      }
    }

    "the user doesn't have sufficient confidence level" - {

      "must redirect the user to the iv uplift url" in {

        whenAuthConnectorReturns(Future.failed(new InsufficientConfidenceLevel))
        val result = invokeHarness()

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value.contains("mdtp/uplift?origin=shortforms&confidenceLevel=250")
      }
    }

    "the user used an unaccepted auth provider" - {

      "must redirect the user to the unauthorised page" in {

        whenAuthConnectorReturns(Future.failed(new UnsupportedAuthProvider))
        val result = invokeHarness()

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.UnauthorisedController.onPageLoad().url
      }
    }

    "the user has an unsupported affinity group" - {

      "must redirect the user to the unauthorised page" in {

        whenAuthConnectorReturns(Future.failed(new UnsupportedAffinityGroup))
        val result = invokeHarness()

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.UnauthorisedController.onPageLoad().url)
      }
    }

    "the user has an unsupported credential role" - {

      "must redirect the user to the unauthorised page" in {

        whenAuthConnectorReturns(Future.failed(new UnsupportedCredentialRole))
        val result = invokeHarness()

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.UnauthorisedController.onPageLoad().url)
      }
    }
  }

  private def invokeHarness() =
    actionUnderTest.invokeBlock(FakeRequest(), (_: Request[?]) => Future.successful(Ok))

  private def whenAuthConnectorReturns(result: Future[?]) =
    when(mockAuthConnector.authorise(any, any)(any, any)) thenReturn result
}
