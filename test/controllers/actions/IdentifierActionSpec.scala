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

import config.FrontendAppConfig
import controllers.Execution.trampoline
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.mvc.Results.*
import play.api.mvc.*
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.auth.core.*
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.Retrieval
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class IdentifierActionSpec extends AnyWordSpec with  Matchers with MockitoSugar with ScalaFutures with GuiceOneAppPerSuite{

  class Harness(authAction: AuthenticatedIdentifierAction) {
    def onPageLoad(): Action[AnyContent] = authAction { request =>
      Ok(s"Success. Internal ID: ${request.id}")

    }
  }
  def customFakeAuthConnector(stubbedResult: Future[_]): AuthConnector = new AuthConnector {
    override def authorise[A](predicate: Predicate, retrieval: Retrieval[A])(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[A] = {
      stubbedResult.map(_.asInstanceOf[A])
    }
  }

  def createTestConfigForUserType(userType: String, ivUrl:String): FrontendAppConfig = {
    val mockConfig = mock[FrontendAppConfig]
    val dummyRule: Predicate = AffinityGroup.Individual and AffinityGroup.Organisation
    val ruleIndividual: Predicate = AffinityGroup.Individual and ConfidenceLevel.L250
    val ruleOrganisation: Predicate = AffinityGroup.Organisation and ConfidenceLevel.L250
    val ruleAgent = AffinityGroup.Agent
    userType match {
      case "Individual" =>
        when(mockConfig.isIndividualWithCL250).thenReturn(ruleIndividual)
        when(mockConfig.isOrganisationWithCL250).thenReturn(dummyRule)
        when(mockConfig.isEnrolledAgent).thenReturn(dummyRule)

      case "organisation" =>
        when(mockConfig.isIndividualWithCL250).thenReturn(dummyRule)
        when(mockConfig.isOrganisationWithCL250).thenReturn(ruleOrganisation)
        when(mockConfig.isEnrolledAgent).thenReturn(dummyRule)
      case "Agent" =>
        when(mockConfig.isIndividualWithCL250).thenReturn(dummyRule)
        when(mockConfig.isOrganisationWithCL250).thenReturn(dummyRule)
        when(mockConfig.isEnrolledAgent).thenReturn(ruleAgent)
    }
    when(mockConfig.identityValidationFrontEndEntry(any(),any())).thenReturn(ivUrl)
    mockConfig
  }

  trait TestSetup {
    val bodyParsers: BodyParsers.Default = new BodyParsers.Default(stubPlayBodyParsers(app.materializer))
    val internalId = "test-internal-id"
    val iVTargetUrl = "http:localhost:9948/iv-stub"

  }

  "AuthenticatedIdentifierAction" when {

    "the user is an Individual with CL250" must {
      "authorise successfully and return Ok" in new TestSetup {
      val config: FrontendAppConfig = createTestConfigForUserType("Individual", iVTargetUrl)
      val authConnector: AuthConnector = customFakeAuthConnector(Future.successful(Some(internalId)))
      val action = new AuthenticatedIdentifierAction(authConnector, config, bodyParsers)
      val controller = new Harness(action)
        val result: Future[Result] = controller.onPageLoad()(FakeRequest("GET", "/sa1"))
        status(result) mustBe OK

      }
    }

    "the user is an Organisation with CL250" must {
      "authorise successfully and return OK" in new TestSetup {

        val config: FrontendAppConfig = createTestConfigForUserType("organisation", iVTargetUrl)
        val authConnector: AuthConnector = customFakeAuthConnector(Future.successful(Some(internalId)))
        val action = new AuthenticatedIdentifierAction(authConnector, config, bodyParsers)
        val controller = new Harness(action)
        val result: Future[Result] = controller.onPageLoad()(FakeRequest("GET", "/sa1"))
        status(result) mustBe OK
      }
    }

     "the user is an Agent with the correct enrollment " must {
      "authorise successfully and return OK" in new TestSetup {

       val config: FrontendAppConfig = createTestConfigForUserType("Agent", iVTargetUrl)
        val authConnector: AuthConnector = customFakeAuthConnector(Future.successful(Some(internalId)))
        val action = new AuthenticatedIdentifierAction(authConnector, config, bodyParsers)
        val controller = new Harness(action)
        val result: Future[Result] = controller.onPageLoad()(FakeRequest("GET", "/sa1"))
        status(result) mustBe OK
      }
    }

     "an Individual fails the CL250 confidence level requirement" must {
      "redirect to the Identity Verification (IV) handle journey" in new TestSetup {

        val config: FrontendAppConfig = createTestConfigForUserType("Individual", iVTargetUrl)
        val authConnector: AuthConnector = customFakeAuthConnector(Future.failed(InsufficientConfidenceLevel()))
        val action = new AuthenticatedIdentifierAction(authConnector, config, bodyParsers)
        val controller = new Harness(action)
        val result: Future[Result] = controller.onPageLoad()(FakeRequest("GET", "/sa1"))
        status(result) mustBe SEE_OTHER
        redirectLocation(result).get mustBe iVTargetUrl
      }
    }

    "an Organization fails the CL250 confidence level requirement" must {
      "redirect to the Identity Verification (IV) handle journey"  in new TestSetup {

        val config: FrontendAppConfig = createTestConfigForUserType("organisation", iVTargetUrl)
        val authConnector: AuthConnector = customFakeAuthConnector(Future.failed(InsufficientConfidenceLevel()))
        val action = new AuthenticatedIdentifierAction(authConnector, config, bodyParsers)
        val controller = new Harness(action)
        val result: Future[Result] = controller.onPageLoad()(FakeRequest("GET", "/sa1"))
        status(result) mustBe SEE_OTHER
        redirectLocation(result).get mustBe iVTargetUrl
      }
    }


      "an Agent logs in with the wrong /missing enrollment " must {
     "fail the predicate and handle it as an AuthorizationExciting" in new TestSetup {

       val config: FrontendAppConfig = createTestConfigForUserType("Agent", iVTargetUrl)
       val authConnector: AuthConnector = customFakeAuthConnector(Future.failed(InsufficientEnrolments()))
       val action = new AuthenticatedIdentifierAction(authConnector, config, bodyParsers)
       val controller = new Harness(action)
       val result: Future[Result] = controller.onPageLoad()(FakeRequest("GET", "/sa1"))
       status(result) mustBe SEE_OTHER
       redirectLocation(result).get mustBe controllers.routes.UnauthorisedController.onPageLoad().url
     }
   }
  }

}
