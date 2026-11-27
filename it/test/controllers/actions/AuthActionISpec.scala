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
import play.api.Logger
import play.api.mvc.{Action, AnyContent, Results}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.auth.core.*
import uk.gov.hmrc.auth.core.AffinityGroup.{Agent, Individual, Organisation}
import uk.gov.hmrc.auth.core.ConfidenceLevel.{L200, L250, L50}
import uk.gov.hmrc.auth.core.retrieve.~ as And
import uk.gov.hmrc.play.bootstrap.tools.LogCapturing

import scala.concurrent.Future
import scala.util.Random

class AuthActionISpec extends ISpecBase with MockitoSugar with LogCapturing {
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

  "Auth Action must" - {

    "redirect the user to login when" - {

      "there is no active session" in {
        when(mockAuthConnector.authorise(any, any)(any, any)) thenReturn Future.failed(new MissingBearerToken)

        val result = Harness.onPageLoad()(FakeRequest())

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value must startWith(appConfig.loginUrl)
      }

      "session has expired" in {
        when(mockAuthConnector.authorise(any, any)(any, any)) thenReturn Future.failed(new BearerTokenExpired)

        val result = Harness.onPageLoad()(FakeRequest())

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value must startWith(appConfig.loginUrl)
      }
    }

    "redirect to Identity Verification when" - {

      "the user is an Individual or Organisation with Confidence Level < 250" in
        withCaptureOfLoggingFrom(Logger(classOf[AuthenticatedIdentifierAction])) { logs =>
          val givenRetrieval = buildRetrieval(
            affinityGroup = if Random.nextBoolean() then Individual else Organisation,
            confidenceLevel = L200
          )
          when(mockAuthConnector.authorise(any, any)(any, any)) thenReturn Future.successful(givenRetrieval)

          val result = Harness.onPageLoad()(FakeRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value.contains("mdtp/uplift?origin=shortform&confidenceLevel=250")

          logs                 must have size 2
          logs.head.getMessage must include("Insufficient Confidence Level")
          logs(1).getMessage   must include("Redirect to IV uplift")
        }
    }

    "redirect the user to the form not available page (ERR-AGENT) when" - {

      "the user is an Agent missing a required enrolment" in {
        val givenRetrieval = buildRetrieval(
          affinityGroup = Agent
        )
        when(mockAuthConnector.authorise(any, any)(any, any)) thenReturn Future.successful(givenRetrieval)

        val result = Harness.onPageLoad()(FakeRequest())

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.FormNotAvailableAgentsController.onPageLoad().url
      }
    }

    "invoke block when" - {

      "user is an Individual or Organisation with Confidence Level 250" in {
        val givenRetrieval = buildRetrieval(
          affinityGroup = if Random.nextBoolean() then Individual else Organisation,
          confidenceLevel = L250
        )
        when(mockAuthConnector.authorise(any, any)(any, any)) thenReturn Future.successful(givenRetrieval)

        val result = Harness.onPageLoad()(FakeRequest())
        status(result) mustBe OK
      }

      "user is an Agent with a required enrolment" in {
        val givenRetrieval = buildRetrieval(
          affinityGroup = Agent,
          roles = Set("IR-SA-AGENT")
        )
        when(mockAuthConnector.authorise(any, any)(any, any)) thenReturn Future.successful(givenRetrieval)

        val result = Harness.onPageLoad()(FakeRequest())
        status(result) mustBe OK
      }
    }

    "redirect the user to the unauthorised page when" - {

      "the user used an unaccepted auth provider" in {
        when(mockAuthConnector.authorise(any, any)(any, any)) thenReturn Future.failed(new UnsupportedAuthProvider)

        val result = Harness.onPageLoad()(FakeRequest())

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.UnauthorisedController.onPageLoad().url
      }

      "the user has an unsupported credential role" in {
        when(mockAuthConnector.authorise(any, any)(any, any)) thenReturn Future.failed(new UnsupportedCredentialRole)

        val result = Harness.onPageLoad()(FakeRequest())

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.UnauthorisedController.onPageLoad().url)
      }
    }
  }

  private def buildRetrieval(
    affinityGroup: AffinityGroup,
    confidenceLevel: ConfidenceLevel = L50,
    roles: Set[String] = Set.empty
  ) = {
    val enrolments = Enrolments(roles map Enrolment.apply)
    val niNumber   = None
    Some(affinityGroup) ~ confidenceLevel ~ enrolments ~ Some("fake_internal_id") ~ niNumber
  }

  extension [A](a: A) {
    def ~[B](b: B): And[A, B] = And(a, b)
  }
}
