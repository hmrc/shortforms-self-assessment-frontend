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
import com.github.tomakehurst.wiremock.client.WireMock.{equalTo, okJson, postRequestedFor, stubFor, urlEqualTo, verify}
import models.sa1.{SA1Address, SA1Reasons, SA1Response, SA1Submission}
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.http.test.WireMockSupport
import uk.gov.hmrc.http.{Authorization, HeaderCarrier, SessionId}

import java.time.{Instant, LocalDate}

class SA1SubmissionConnectorISpec extends ISpecBase with WireMockSupport {

  override def fakeApplication(): Application =
    new GuiceApplicationBuilder()
      .configure(
        "microservice.services.shortforms-self-assessment.port" -> wireMockPort
      )
      .build()

  private val submitUrl = "/self-assessment-forms/api/sa1"

  private val submission = SA1Submission(
    firstName = "Jane",
    lastName = "Doe",
    previousLastName = None,
    niNumber = Some("AB123456C"),
    niNoReason = None,
    dob = LocalDate.of(1990, 1, 1),
    address = SA1Address(line1 = "1 Street", line2 = "", town = Some("Town"), postcode = "AA1 1AA", country = None),
    moveToAddressOn = LocalDate.of(2020, 1, 1),
    phone = "07123456789",
    email = "jane@example.com",
    reasons = SA1Reasons(),
    dateLastNameChanged = None
  )

  private lazy val connector = app.injector.instanceOf[SA1SubmissionConnector]

  "submit" - {

    "must forward the Authorization and X-Session-ID headers to the backend" in {
      stubFor(
        WireMock
          .post(urlEqualTo(submitUrl))
          .willReturn(okJson("""{"acknowledgementRef":"ref-123","receivedTimestamp":"2026-11-13T14:18:34.301Z"}"""))
      )

      val hc = HeaderCarrier(
        authorization = Some(Authorization("Bearer token-123")),
        sessionId = Some(SessionId("session-456"))
      )

      connector.submit(submission)(hc).futureValue mustBe SA1Response(
        "ref-123",
        Instant.parse("2026-11-13T14:18:34.301Z")
      )

      verify(
        postRequestedFor(urlEqualTo(submitUrl))
          .withHeader("Authorization", equalTo("Bearer token-123"))
          .withHeader("X-Session-ID", equalTo("session-456"))
      )
    }
  }
}
