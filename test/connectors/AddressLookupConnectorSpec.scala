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
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import org.scalatest.BeforeAndAfterAll
import play.api.libs.json.Json
import play.api.test.Helpers.running
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}

class AddressLookupConnectorSpec extends SpecBase with BeforeAndAfterAll {
  private val server                     = new WireMockServer(wireMockConfig().dynamicPort())
  implicit private val hc: HeaderCarrier = HeaderCarrier()

  override def beforeAll(): Unit = {
    super.beforeAll()
    server.start()
  }

  override def afterAll(): Unit =
    try server.stop()
    finally super.afterAll()

  private def withConnector(test: AddressLookupConnector => Unit): Unit = {
    server.resetAll()
    val app = applicationBuilder()
      .configure(
        "microservice.services.address-lookup-frontend.port" -> server.port()
      )
      .build()
    running(app) {
      test(app.injector.instanceOf[AddressLookupConnector])
    }
  }

  "ALF connector" - {
    "must fail explicitly if ALF omits its Location header" in withConnector { connector =>
      server.stubFor(post(urlEqualTo("/api/v2/init")).willReturn(aResponse().withStatus(202)))
      connector.initialise(Json.obj()).failed.futureValue mustBe a[IllegalStateException]
    }

    Seq(200, 400, 500).foreach { status =>
      s"must reject an unexpected $status initialisation response" in withConnector { connector =>
        server.stubFor(
          post(urlEqualTo("/api/v2/init"))
            .willReturn(aResponse().withStatus(status).withHeader("Location", "http://localhost:9028/not-accepted"))
        )
        connector.initialise(Json.obj()).failed.futureValue mustBe a[UpstreamErrorResponse]
      }
    }

    "must retrieve the confirmed address using an encoded query parameter, preserving the UPRN and town" in withConnector {
      connector =>
        val id     = "id with & symbols"
        server.stubFor(
          get(urlPathEqualTo("/api/v2/confirmed"))
            .withQueryParam("id", equalTo(id))
            .willReturn(
              okJson(
                Json
                  .obj(
                    "auditRef" -> "audit-ref",
                    "uprn"     -> 123456789L,
                    "address"  -> Json
                      .obj("lines" -> Seq("A long address line that must never be truncated"), "town" -> "Town")
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

    "must propagate confirmed-address retrieval failures" in withConnector { connector =>
      server.stubFor(get(urlPathEqualTo("/api/v2/confirmed")).willReturn(aResponse().withStatus(404)))
      connector.getAddress("missing").failed.futureValue mustBe a[UpstreamErrorResponse]
    }

    "must fail if the confirmed address response cannot be decoded" in withConnector { connector =>
      server.stubFor(get(urlPathEqualTo("/api/v2/confirmed")).willReturn(okJson("""{"address":false}""")))
      connector.getAddress("invalid").failed.futureValue mustBe a[Exception]
    }
  }
}
