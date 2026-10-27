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

import config.FrontendAppConfig
import models.address.ConfirmedAddress
import play.api.http.Status.ACCEPTED
import play.api.libs.json.JsObject
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps, UpstreamErrorResponse}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AddressLookupConnector @Inject() (http: HttpClientV2, appConfig: FrontendAppConfig)(implicit
  ec: ExecutionContext
) {

  def initialise(configuration: JsObject)(implicit hc: HeaderCarrier): Future[String] =
    http
      .post(url"${appConfig.addressLookupBaseUrl}/api/v2/init")
      .withBody(configuration)
      .execute[HttpResponse]
      .flatMap { response =>
        if (response.status != ACCEPTED) {
          Future.failed(UpstreamErrorResponse("Unexpected ALF initialisation response", response.status))
        } else {
          response.header("Location").filter(_.nonEmpty) match {
            case Some(location) => Future.successful(location)
            case None           => Future.failed(new IllegalStateException("ALF response is missing its Location header"))
          }
        }
      }

  def getAddress(id: String)(implicit hc: HeaderCarrier): Future[ConfirmedAddress] =
    http
      .get(url"${appConfig.addressLookupBaseUrl}/api/v2/confirmed?id=$id")
      .execute[ConfirmedAddress]
}
