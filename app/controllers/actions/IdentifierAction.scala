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

import com.google.inject.Inject
import config.FrontendAppConfig
import controllers.routes
import models.requests.IdentifierRequest
import play.api.Logging
import play.api.mvc.*
import play.api.mvc.Results.*
import uk.gov.hmrc.auth.core.*
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendHeaderCarrierProvider

import scala.concurrent.{ExecutionContext, Future}

trait IdentifierAction extends ActionBuilder[IdentifierRequest, AnyContent]

class AuthenticatedIdentifierAction @Inject() (
  override val authConnector: AuthConnector,
  config: FrontendAppConfig,
  val parser: BodyParsers.Default
)(implicit val executionContext: ExecutionContext)
    extends IdentifierAction
    with AuthorisedFunctions
    with FrontendHeaderCarrierProvider
    with Logging {

  override def invokeBlock[A](implicit
    request: Request[A],
    block: IdentifierRequest[A] => Future[Result]
  ): Future[Result] = {
    given RequestHeader = request

    authorised().retrieve(
      Retrievals.affinityGroup and
        Retrievals.confidenceLevel and
        Retrievals.allEnrolments and
        Retrievals.internalId and
        Retrievals.nino
    ) { case affinityGroup ~ confidenceLevel ~ enrolments ~ internalId ~ nino =>
      affinityGroup.get match

        case AffinityGroup.Individual | AffinityGroup.Organisation =>
          if confidenceLevel.level >= config.requiredConfidenceLevel
          then block(new IdentifierRequest(request, internalId.get, nino))
          else
            logger.info("[AuthenticatedIdentifierAction] Insufficient Confidence Level")
            throw InsufficientConfidenceLevel(s"Confidence level below ${config.requiredConfidenceLevel}")

        case AffinityGroup.Agent =>
          val hasValidAgentEnrolment = config.agentEnrolments.exists(enrolmentName =>
            enrolments
              .getEnrolment(enrolmentName)
              .exists(_.isActivated)
          )

          if hasValidAgentEnrolment
          then block(new IdentifierRequest(request, internalId.get, None))
          else
            logger.info("[AuthenticatedIdentifierAction] Insufficient Agent Enrolments")
            throw InsufficientEnrolments("The agent has no valid enrolments")
    }
  } recover {
    case _: NoActiveSession => Redirect(config.loginUrl, Map("continue" -> Seq(getRequestURI)))

    case _: InsufficientConfidenceLevel =>
      val successURL  = getRequestURI
      val failureURL  = config.host + controllers.identityVerification.routes.IvController.handleIvFailure(None).url
      val queryParams = Map(
        "origin"          -> Seq("shortform"),
        "confidenceLevel" -> Seq("250"),
        "completionURL"   -> Seq(successURL),
        "failureURL"      -> Seq(failureURL)
      )
      logger.info(
        s"[AuthenticatedIdentifierAction] Redirect to IV uplift with success URL: + $successURL; failure URL: $failureURL"
      )

      Redirect(config.identityVerificationFrontend, queryParams)

    case _: InsufficientEnrolments =>
      Redirect(routes.FormNotAvailableAgentsController.onPageLoad())

    case _ =>
      Redirect(routes.UnauthorisedController.onPageLoad())
  }

  private def getRequestURI(using rh: RequestHeader): String = s"${config.host}${rh.uri}"
}
