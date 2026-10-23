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

import base.ISpecBase
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, okJson, stubFor, urlEqualTo}
import models.identityVerification.IvResultType
import org.scalatest.RecoverMethods._
import play.api.Application
import play.api.http.Status.NOT_FOUND
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.http.test.WireMockSupport
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}

class IvConnectorISpec extends ISpecBase with WireMockSupport {

  override def fakeApplication(): Application =
    new GuiceApplicationBuilder()
      .configure(
        "microservice.services.identity-verification.port" -> wireMockPort
      )
      .build()

  override def beforeEach(): Unit = {
    super.beforeEach()
    WireMock.reset()
  }

  implicit lazy val hc: HeaderCarrier = HeaderCarrier()

  private lazy val connector =
    app.injector.instanceOf[IvConnector]

  ".getJourneyStatus" - {
    "must return the journey result when the origin is shortform" in {

      stubFor(
        WireMock
          .get(urlEqualTo("/identity-verification/journey/journey-123"))
          .willReturn(
            okJson(
              """
                    {
                      "serviceContract": {
                        "origin": "shortform"
                      },
                      "progress": {
                        "result": "TechnicalIssue"
                      }
                    }
                  """
            )
          )
      )

      val result = connector.getJourneyStatus("journey-123").futureValue

      result mustBe IvResultType.TechnicalIssue
    }

    "must fail with NOT_FOUND when the origin is not shortform" in {

      stubFor(
        WireMock
          .get(urlEqualTo("/identity-verification/journey/journey-123"))
          .willReturn(
            okJson(
              """
                  {
                    "serviceContract": {
                      "origin": "another-origin"
                    },
                    "progress": {
                      "result": "TechnicalIssue"
                    }
                  }
                  """
            )
          )
      )

      val exception =
        recoverToExceptionIf[UpstreamErrorResponse] {
          connector.getJourneyStatus("journey-123")
        }.futureValue

      exception.statusCode mustBe NOT_FOUND
      exception.message mustBe "Identity Verification result not found."
    }

    "must propagate an upstream error response" in {

      stubFor(
        WireMock
          .get(urlEqualTo("/identity-verification/journey/journey-123"))
          .willReturn(
            aResponse()
              .withStatus(500)
          )
      )

      val exception =
        recoverToExceptionIf[UpstreamErrorResponse] {
          connector.getJourneyStatus("journey-123")
        }.futureValue

      exception.statusCode mustBe 500
    }
  }
}
