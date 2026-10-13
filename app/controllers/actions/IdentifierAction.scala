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
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.http.UnauthorizedException
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendHeaderCarrierProvider
import uk.gov.hmrc.http.{HeaderCarrier, UnauthorizedException}
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import uk.gov.hmrc.play.bootstrap.binders.RedirectUrl

import scala.concurrent.{ExecutionContext, Future}

trait IdentifierAction extends ActionBuilder[IdentifierRequest, AnyContent] with ActionFunction[Request, IdentifierRequest]

class AuthenticatedIdentifierAction @Inject()(
                                               override val authConnector: AuthConnector,
                                               config: FrontendAppConfig,
                                               val parser: BodyParsers.Default
                                             )(implicit val executionContext: ExecutionContext)
  extends IdentifierAction
    with AuthorisedFunctions
    with FrontendHeaderCarrierProvider with Logging {

  private val allowedAgentEnrolments: Seq[String] = Seq("IR-SA-AGENT", "IR-CT-AGENT", "HMCE-VAT-AGNT", "HMRC-MGD-AGNT", "HMRC-AS-AGENT")
  private val isIndividualWithCL250: Predicate = AffinityGroup.Individual and ConfidenceLevel.L250
  private val isOrganisationWithCL250: Predicate = AffinityGroup.Organisation and ConfidenceLevel.L250
  private val hasAgentEnrolment: Predicate =
    allowedAgentEnrolments
      .map(name => Enrolment(name, Seq.empty, "activated"))
      .reduceLeft[Predicate](_ or _)
  private val isEnrolledAgent: Predicate = AffinityGroup.Agent and hasAgentEnrolment

  override def invokeBlock[A](request: Request[A], block: IdentifierRequest[A] => Future[Result]): Future[Result] = {
    given Request[?] = request


    val predicates =  isIndividualWithCL250 or isOrganisationWithCL250 or isEnrolledAgent

    authorised(predicates).retrieve(Retrievals.internalId) {
      _.map {
        internalId => block(IdentifierRequest(request, internalId))
      }.getOrElse(throw new UnauthorizedException("Unable to retrieve internal Id"))
    } recover {
      case _: NoActiveSession => Redirect(config.loginUrl, Map("continue" -> Seq(getRequestURI)))

      case _: InsufficientConfidenceLevel =>
        val queryParams = Map(
          "origin"          -> Seq("shortforms"),
          "confidenceLevel" -> Seq("250"),
          "completionURL"   -> Seq(getRequestURI),
          "failureURL"      -> Seq(routes.UnauthorisedController.onPageLoad().url),
        )

        val loggedCompletionUrl = queryParams.get("completionURL").flatMap(_.headOption).getOrElse("Unknown")
        val loggedFailureUrl = queryParams.get("failureURL").flatMap(_.headOption).getOrElse("Unknown")

        logger.info(s"[AuthenticatedIdentifierAction][invokeBlock] Insufficient Confidence Level. "+
          s"Redirecting to ivUplift service with successful URL: + ${loggedCompletionUrl} and failure ULR: ${loggedFailureUrl}L")
        Redirect(config.identityVerificationFrontend, queryParams)

    case _ =>
        Redirect(routes.UnauthorisedController.onPageLoad())
    }
  }

  private def getRequestURI(using req: Request[?]) =
    if req.host startsWith "localhost" then
      val protocol = if req.secure then "https" else "http"
      s"$protocol://${req.host}${req.uri}"
    else
      req.uri
}
