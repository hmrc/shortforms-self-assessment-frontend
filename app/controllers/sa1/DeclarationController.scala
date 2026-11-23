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

import connectors.SA1SubmissionConnector
import controllers.{AppController, AppControllerComponents}
import forms.sa1.DeclarationFormProvider
import models.{SubmissionConfirmation, UserAnswers}
import pages.sa1.DeclarationPage
import play.api.Logging
import play.api.mvc.{Action, AnyContent, Request, Result}
import repositories.SessionRepository
import services.SA1SubmissionMapper
import uk.gov.hmrc.http.HeaderCarrier
import utils.ErrorReference
import views.html.SystemErrorView
import views.html.sa1.DeclarationView

import javax.inject.Inject
import scala.concurrent.Future
import scala.util.control.NonFatal

class DeclarationController @Inject() (
  val controllerComponents: AppControllerComponents,
  sessionRepository: SessionRepository,
  formProvider: DeclarationFormProvider,
  submissionConnector: SA1SubmissionConnector,
  view: DeclarationView,
  systemErrorView: SystemErrorView
) extends AppController
    with Logging {

  private val form = formProvider()

  def onPageLoad(journeyId: String): Action[AnyContent] =
    SA1DataAction(journeyId) { implicit request =>
      val preparedForm = request.userAnswers.get(DeclarationPage).fold(form)(form.fill)
      Ok(view(preparedForm, journeyId))
    }

  def onSubmit(journeyId: String): Action[AnyContent] =
    SA1DataAction(journeyId).async { implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, journeyId))),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(DeclarationPage, value))
              _              <- sessionRepository.set(updatedAnswers)
              result         <- submitSA1(journeyId, updatedAnswers)
            } yield result
        )
    }

  private def submitSA1(journeyId: String, answers: UserAnswers)(implicit
    request: Request[?],
    hc: HeaderCarrier
  ): Future[Result] =
    SA1SubmissionMapper.map(answers) match {
      case Left(error)       =>
        val reference = ErrorReference.generate()
        logger.error(s"[DeclarationController][submitSA1] reference=$reference Unable to build SA1 submission: $error")
        Future.successful(InternalServerError(systemErrorView(Some(reference))))
      case Right(submission) =>
        submissionConnector
          .submit(submission)
          .flatMap { response =>
            val confirmation = SubmissionConfirmation(response.acknowledgementRef, response.receivedTimestamp)
            sessionRepository
              .set(answers.copy(confirmation = Some(confirmation)))
              .map(_ => Redirect(routes.ConfirmationController.onPageLoad(journeyId)))
          }
          .recover { case NonFatal(e) =>
            val reference = ErrorReference.generate()
            logger.error(s"[DeclarationController][submitSA1] reference=$reference SA1 submission failed", e)
            BadGateway(systemErrorView(Some(reference)))
          }
    }
}
