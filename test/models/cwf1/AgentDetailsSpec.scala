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

package models.cwf1

import base.SpecBase
import play.api.libs.json.Json

class AgentDetailsSpec extends SpecBase {

  "AgentDetails" - {
    val agentDetails: AgentDetails = AgentDetails(fullName = "Mary Jane", clientReference = Some("1234567890"))

    "must round-trip through JSON" in {
      Json.toJson(agentDetails).as[AgentDetails] mustEqual agentDetails
    }

    "must serialise with firstName and lastName keys" in {
      Json.toJson(agentDetails) mustEqual
        Json.obj("clientReference" -> "1234567890", "fullName" -> "Mary Jane")
    }
  }
}
