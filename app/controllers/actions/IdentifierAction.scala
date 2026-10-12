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
import play.api.mvc.*
import play.api.mvc.Results.*
import uk.gov.hmrc.auth.core.*
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
    with FrontendHeaderCarrierProvider {

  override def invokeBlock[A](request: Request[A], block: IdentifierRequest[A] => Future[Result]): Future[Result] = {
    given Request[?] = request

    val predicates =  config.isIndividualWithCL250 or config.isOrganisationWithCL250 or config.isEnrolledAgent

    authorised(predicates).retrieve(Retrievals.internalId) {
      _.map {
        internalId => block(IdentifierRequest(request, internalId))
      }.getOrElse(throw new UnauthorizedException("Unable to retrieve internal Id"))
    } recover {
      case _: NoActiveSession => Redirect(config.loginUrl, Map("continue" -> Seq(getRequestURI)))

      case _: InsufficientConfidenceLevel =>
        val completionURL = RedirectUrl(request.uri)
        val failureURL = RedirectUrl(routes.UnauthorisedController.onPageLoad().url)
        val url = config.identityValidationFrontEndEntry(completionURL, failureURL)
        SeeOther(url)
      case _: AuthorisationException => Redirect(routes.UnauthorisedController.onPageLoad())
    }
  }

  private def getRequestURI(using req: Request[?]) =
    if req.host startsWith "localhost" then
      val protocol = if req.secure then "https" else "http"
      s"$protocol://${req.host}${req.uri}"
    else
      req.uri
}
