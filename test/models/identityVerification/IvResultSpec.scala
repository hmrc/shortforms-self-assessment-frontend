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

package models.identityVerification

import base.SpecBase
import play.api.libs.json.{JsError, JsSuccess, Json}

class IvResultSpec extends SpecBase {
  "IvResult" - {
    ".reads" - {
      "must parse a valid IV result" in {
        val json = Json.parse(
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

        json.validate[IvResult] mustBe JsSuccess(
          IvResult(
            origin = "shortform",
            result = IvResultType.TechnicalIssue
          )
        )
      }

      "must parse a valid IV result from a full IV response" in {
        val json = Json.parse(
          """
              {
                "journeyId": "6ab81557-499c-4704-8438-1f802c0db40e",
                "journeyType": "UpliftNoNino",
                "progress": {
                  "questions": [],
                  "answerDataCache": null,
                  "p60questions": null,
                  "numAnswered": 0,
                  "result": "TechnicalIssue",
                  "insufficientEvidenceEvidences": null,
                  "verificationMethod": null,
                  "hasPhotoId": null,
                  "hasSmartphoneWithCamera": null
                },
                "serviceContract": {
                  "origin": "shortform",
                  "completionURL": "/shortforms/form/SA1",
                  "failureURL": "/shortforms/upliftFailure",
                  "confidenceLevel": 250
                },
                "nino": null,
                "availableEvidenceOptions": [],
                "gdprConsentAsked": null,
                "self": "http://localhost:9927/identity-verification/journey/6ab81557-499c-4704-8438-1f802c0db40e",
                "start": "http://localhost:9927/identity-verification/journey/6ab81557-499c-4704-8438-1f802c0db40e/start",
                "additionalData": null,
                "availableQuestions": [],
                "correlationId": null,
                "saPensionLevel": null,
                "dcsFirstJourney": true,
                "openBankingJourney": false,
                "authProviderId": "9554690765897020"
              }
            """
        )

        json.validate[IvResult] mustBe JsSuccess(
          IvResult(
            origin = "shortform",
            result = IvResultType.TechnicalIssue
          )
        )
      }

      "must fail when origin is missing" in {
        val json = Json.parse(
          """
              {
                "serviceContract": {},
                "progress": {
                  "result": "Success"
                }
              }
            """
        )

        json.validate[IvResult] mustBe a[JsError]
      }

      "must fail when result is missing" in {
        val json = Json.parse(
          """
              {
                "serviceContract": {
                  "origin": "shortform"
                },
                "progress": {}
              }
            """
        )

        json.validate[IvResult] mustBe a[JsError]
      }

      "must fail when origin is not a string" in {
        val json = Json.parse(
          """
              {
                "serviceContract": {
                  "origin": 123
                },
                "progress": {
                  "result": "Success"
                }
              }
            """
        )

        json.validate[IvResult] mustBe a[JsError]
      }

      "must convert the result string into an IvResultType" in {
        val json = Json.parse(
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

        json.validate[IvResult].get.result mustBe IvResultType.TechnicalIssue
      }
    }
  }
}
