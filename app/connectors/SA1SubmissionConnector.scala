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

package connectors

import com.google.inject.{Inject, Singleton}
import config.FrontendAppConfig
import models.sa1.{SA1Response, SA1Submission}
import play.api.Logging
import play.api.libs.json.Json
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps, UpstreamErrorResponse}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Failure

@Singleton
class SA1SubmissionConnector @Inject() (appConfig: FrontendAppConfig, httpClient: HttpClientV2)(implicit
  ec: ExecutionContext
) extends Logging {

  private val baseUrl = appConfig.shortFormsSelfAssessmentBaseUrl

  def submit(submission: SA1Submission)(implicit hc: HeaderCarrier): Future[SA1Response] =
    // Authorization and X-Session-ID are forwarded from hc by http-verbs for internal hosts, so no headers are set here.
    httpClient
      .post(url"$baseUrl/self-assessment-forms/api/sa1")
      .withBody(Json.toJson(submission))
      .execute[Either[UpstreamErrorResponse, SA1Response]]
      .flatMap {
        case Right(response) => Future.successful(response)
        case Left(error)     => Future.failed(error)
      }
      .andThen { case Failure(e) =>
        logger.error("[SA1SubmissionConnector][submit] Failed to submit SA1 form", e)
      }
}
