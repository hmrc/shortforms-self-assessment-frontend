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

import base.SpecBase
import com.github.tomakehurst.wiremock.client.WireMock.*
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import uk.gov.hmrc.http.test.WireMockSupport
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}

class AddressLookupConnectorISpec extends SpecBase with GuiceOneAppPerSuite with WireMockSupport {

  override def fakeApplication(): Application =
    new GuiceApplicationBuilder()
      .configure("microservice.services.address-lookup-frontend.port" -> wireMockPort)
      .build()

  implicit private lazy val hc: HeaderCarrier = HeaderCarrier()

  private lazy val connector = app.injector.instanceOf[AddressLookupConnector]

  "ALF connector" - {
    "must fail explicitly if ALF omits its Location header" in {
      stubFor(post(urlEqualTo("/api/v2/init")).willReturn(aResponse().withStatus(202)))

      connector.initialise(Json.obj()).failed.futureValue mustBe a[IllegalStateException]
    }

    Seq(200, 400, 500).foreach { status =>
      s"must reject an unexpected $status initialisation response" in {
        stubFor(
          post(urlEqualTo("/api/v2/init"))
            .willReturn(aResponse().withStatus(status).withHeader("Location", "http://localhost:9028/not-accepted"))
        )

        connector.initialise(Json.obj()).failed.futureValue mustBe a[UpstreamErrorResponse]
      }
    }

    "must retrieve the confirmed address using an encoded query parameter, preserving the UPRN and town" in {
      val id = "id with & symbols"
      stubFor(
        get(urlPathEqualTo("/api/v2/confirmed"))
          .withQueryParam("id", equalTo(id))
          .willReturn(
            okJson(
              Json
                .obj(
                  "auditRef" -> "audit-ref",
                  "uprn"     -> 123456789L,
                  "address"  -> Json.obj(
                    "lines" -> Seq("A long address line that must never be truncated"),
                    "town"  -> "Town"
                  )
                )
                .toString
            )
          )
      )

      val result = connector.getAddress(id).futureValue

      result.uprn mustBe Some(123456789L)
      result.address.town mustBe Some("Town")
      result.address.lines.head mustBe "A long address line that must never be truncated"
    }

    "must propagate confirmed-address retrieval failures" in {
      stubFor(get(urlPathEqualTo("/api/v2/confirmed")).willReturn(aResponse().withStatus(404)))

      connector.getAddress("missing").failed.futureValue mustBe a[UpstreamErrorResponse]
    }

    "must fail if the confirmed address response cannot be decoded" in {
      stubFor(get(urlPathEqualTo("/api/v2/confirmed")).willReturn(okJson("""{"address":false}""")))

      connector.getAddress("invalid").failed.futureValue mustBe a[Exception]
    }
  }
}
