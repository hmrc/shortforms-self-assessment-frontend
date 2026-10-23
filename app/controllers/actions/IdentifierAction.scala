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
import uk.gov.hmrc.http.UnauthorizedException

import scala.concurrent.{ExecutionContext, Future}

trait IdentifierAction
    extends ActionBuilder[IdentifierRequest, AnyContent]
    with ActionFunction[Request, IdentifierRequest]

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
    ) {

      case Some(affinityGroup) ~ confidenceLevel ~ enrolments ~ Some(internalId) ~ nino =>
        val hasValidAgentEnrolment = config.agentEnrolments.exists(enrolmentName =>
          enrolments
            .getEnrolment(enrolmentName)
            .exists(_.isActivated)
        )

        (affinityGroup, hasValidAgentEnrolment, confidenceLevel.level) match {
          case (AffinityGroup.Agent, true, _) =>
            block(new IdentifierRequest(request, internalId, None))

          case (AffinityGroup.Individual, _, cl) if cl >= config.requiredConfidenceLevel =>
            block(new IdentifierRequest(request, internalId, nino))

          case (AffinityGroup.Organisation, _, cl) if cl >= config.requiredConfidenceLevel =>
            block(new IdentifierRequest(request, internalId, nino))

          case (AffinityGroup.Individual, _, _) | (AffinityGroup.Organisation, _, _) =>
            logger.info(
              s"[AuthenticatedIdentifierAction][invokeBlock] Insufficient Confidence Level below required confidential level"
            )
            throw InsufficientConfidenceLevel(s"Confidence level below ${config.requiredConfidenceLevel}")

          case _ =>
            throw UnsupportedAffinityGroup("Unsupported user type or group configuration")
        }
      case _                                                                            =>
        throw new UnauthorizedException("Unable to retrieve internalId")

    }
  } recover {
    case _: NoActiveSession => Redirect(config.loginUrl, Map("continue" -> Seq(getRequestURI)))

    case _: InsufficientConfidenceLevel =>
      val loggedCompletionUrl = getRequestURI
      val loggedFailureUrl    =
        getHost + controllers.identityVerification.routes.IvController.handleIvFailure(None).url
      val queryParams         = Map(
        "origin"          -> Seq("shortform"),
        "confidenceLevel" -> Seq("250"),
        "completionURL"   -> Seq(loggedCompletionUrl),
        "failureURL"      -> Seq(loggedFailureUrl)
      )
      logger.info(
        s"[AuthenticatedIdentifierAction][invokeBlock] Insufficient Confidence Level. " +
          s"Redirecting to ivUplift service with successful URL: + $loggedCompletionUrl and failure ULR: $loggedFailureUrl"
      )

      Redirect(config.identityVerificationFrontend, queryParams)

    case _ =>
      Redirect(routes.UnauthorisedController.onPageLoad())
  }
}

private def getHost(using rh: RequestHeader): String =
  if rh.host startsWith "localhost" then
    val protocol = if rh.secure then "https" else "http"
    s"$protocol://${rh.host}"
  else ""

private def getRequestURI(using rh: RequestHeader): String =
  s"$getHost${rh.uri}"
