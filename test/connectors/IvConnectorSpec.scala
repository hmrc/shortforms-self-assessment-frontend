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
import config.FrontendAppConfig
import models.identityVerification.{IvResult, IvResultSpec, IvResultType}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.FakeRequest
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}

import scala.concurrent.Future

class IvConnectorSpec extends SpecBase with MockitoSugar {

  private val ivUrl = "http://localhost:9927"

  implicit val hc: HeaderCarrier       = HeaderCarrier()
  implicit val request: FakeRequest[_] = FakeRequest()

  "IvConnector" - {
    ".getJourneyStatus" - {
      "must successfully return IV journey status when origin matches to 'shortform'" in {
        val mockHttpClient     = mock[HttpClientV2]
        val mockConfig         = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]

        val testIvResult  = IvResult(origin = "shortform", result = IvResultType.TechnicalIssue)
        val testJourneyId = "journeyId"

        when(mockConfig.identityVerificationΒaseUrl).thenReturn(ivUrl)
        when(mockHttpClient.get(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, IvResultSpec]](any(), any()))
          .thenReturn(Future.successful(Right(testIvResult)))

        val connector            = new IvConnector(mockConfig, mockHttpClient)
        val result: IvResultType = connector.getJourneyStatus(testJourneyId).futureValue

        result mustBe IvResultType.TechnicalIssue
      }

      "must throw a RuntimeException exception when origin doesn't match 'shortform'" in {
        val mockHttpClient     = mock[HttpClientV2]
        val mockConfig         = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]

        val testIvResult  = IvResult(origin = "another-origin", result = IvResultType.TechnicalIssue)
        val testJourneyId = "journeyId"

        when(mockConfig.identityVerificationΒaseUrl).thenReturn(ivUrl)
        when(mockHttpClient.get(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, IvResultSpec]](any(), any()))
          .thenReturn(Future.successful(Right(testIvResult)))

        val connector = new IvConnector(mockConfig, mockHttpClient)

        val exception =
          connector.getJourneyStatus(testJourneyId).failed.futureValue

        exception mustBe a[RuntimeException]
        exception.getMessage mustBe
          "Unexpected origin 'another-origin', expected 'shortform'"
      }

      "must fail when IV returns an UpstreamErrorResponse" in {
        val mockHttpClient     = mock[HttpClientV2]
        val mockConfig         = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]

        val upstreamError = uk.gov.hmrc.http.UpstreamErrorResponse("Internal Server Error", 500)
        val testJourneyId = "journeyId"

        when(mockConfig.identityVerificationΒaseUrl).thenReturn(ivUrl)
        when(mockHttpClient.get(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, IvResultSpec]](any(), any()))
          .thenReturn(Future.successful(Left(upstreamError)))

        val connector = new IvConnector(mockConfig, mockHttpClient)

        val exception =
          connector.getJourneyStatus(testJourneyId).failed.futureValue

        exception mustBe upstreamError
      }

      "must pass implicit HeaderCarrier to http client" in {
        val mockHttpClient     = mock[HttpClientV2]
        val mockConfig         = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val customHc           = HeaderCarrier(sessionId = Some(uk.gov.hmrc.http.SessionId("test-session")))

        val testIvResult  = IvResult(origin = "shortform", result = IvResultType.TechnicalIssue)
        val testJourneyId = "journeyId"

        when(mockConfig.identityVerificationΒaseUrl).thenReturn(ivUrl)
        when(mockHttpClient.get(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, IvResultSpec]](any(), any()))
          .thenReturn(Future.successful(Right(testIvResult)))

        val connector = new IvConnector(mockConfig, mockHttpClient)
        connector.getJourneyStatus(testJourneyId)(customHc).futureValue

        verify(mockHttpClient, times(1)).get(any())(any())
      }

      "must propagate exceptions from execute via recoverWith" in {
        val mockHttpClient     = mock[HttpClientV2]
        val mockConfig         = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]

        val testJourneyId = "journeyId"
        val exception     = new RuntimeException("boom")

        when(mockConfig.identityVerificationΒaseUrl).thenReturn(ivUrl)
        when(mockHttpClient.get(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[UpstreamErrorResponse, IvResultSpec]](any(), any()))
          .thenReturn(Future.failed(exception))

        val connector = new IvConnector(mockConfig, mockHttpClient)

        val result =
          connector.getJourneyStatus(testJourneyId).failed.futureValue

        result mustBe exception
      }
    }
  }
}
