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
import models.UserAnswers
import pages.sa1.DeclarationPage
import play.api.Logging
import play.api.mvc.{Action, AnyContent, Result}
import repositories.SessionRepository
import services.SA1SubmissionMapper
import uk.gov.hmrc.http.HeaderCarrier
import views.html.sa1.DeclarationView

import javax.inject.Inject
import scala.concurrent.Future
import scala.util.control.NonFatal

class DeclarationController @Inject() (
  val controllerComponents: AppControllerComponents,
  sessionRepository: SessionRepository,
  formProvider: DeclarationFormProvider,
  submissionConnector: SA1SubmissionConnector,
  view: DeclarationView
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
              result         <- submitSA1(updatedAnswers)
            } yield result
        )
    }

  private def submitSA1(answers: UserAnswers)(implicit hc: HeaderCarrier): Future[Result] =
    SA1SubmissionMapper.map(answers) match {
      case Left(error)       =>
        logger.error(s"[DeclarationController][submitSA1] Unable to build SA1 submission: $error")
        Future.successful(Redirect(controllers.routes.SystemErrorController.onPageLoad()))
      case Right(submission) =>
        submissionConnector
          .submit(submission)
          .map { response =>
            Redirect(routes.ConfirmationController.onPageLoad())
              .flashing(
                "submissionReference" -> response.acknowledgementRef,
                "submissionTimestamp" -> response.receivedTimestamp.toString
              )
          }
          .recover { case NonFatal(_) =>
            Redirect(controllers.routes.SystemErrorController.onPageLoad())
          }
    }
}
