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

package Connectors.identityVerification

import com.google.inject.Inject
import config.{FrontendAppConfig, Service}
import models.identityVerification.{IvResult, IvResultType}
import play.api.{Configuration, Logging}
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException, StringContextOps}

import scala.concurrent.{ExecutionContext, Future}

class IvConnector @Inject() (appConfig: FrontendAppConfig, httpClient: HttpClientV2)(implicit ec: ExecutionContext)
    extends Logging {

  private val baseUrl        = appConfig.identityVerificationΒaseUrl
  private val expectedOrigin = "shortform"

  def getJourneyStatus(journeyId: String)(implicit hc: HeaderCarrier): Future[IvResultType] =
    httpClient
      .get(url"$baseUrl/identity-verification/journey/$journeyId")
      .execute[IvResult]
      .map { response =>
        if (response.origin != expectedOrigin) {
          logger.error(
            s"Unexpected IV origin '${response.origin}', expected '$expectedOrigin' for journeyId=$journeyId"
          )
          throw new InternalServerException(
            s"Unexpected origin '${response.origin}', expected '$expectedOrigin'"
          )
        }
        response.result
      }
      .recoverWith { case e =>
        logger.error(
          s"Failed to retrieve IV journey status for journeyId=$journeyId",
          e
        )
        Future.failed(e)
      }
}
