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

package viewmodels.checkAnswers.cwf1

import base.SpecBase
import models.CheckMode
import models.cwf1.AgentDetails
import pages.cwf1.AgentDetailsPage
import play.api.i18n.Messages

class AgentDetailsSummarySpec extends SpecBase {
  private given Messages = play.api.test.Helpers.stubMessages()

  "AgentDetailsSummary" - {
    "must return no rows when agent's details do not exist" in {
      val result =
        AgentDetailsSummary.rows(emptyUserAnswers)

      result mustBe empty
    }

    "must return two rows when agent's details exist" in {
      val agentsDetails = AgentDetails(fullName = "Name", clientReference = Some("12345667"))
      val userAnswers   = emptyUserAnswers.set(AgentDetailsPage, agentsDetails).success.value

      val result =
        AgentDetailsSummary.rows(userAnswers)

      result must have size 2

      result(1).value.content.toString   must include("12345667")
      result.head.value.content.toString must include("Name")

      val expectedUrl =
        controllers.cwf1.routes.AgentDetailsController
          .onPageLoad(userAnswersId, CheckMode)
          .url

      result.head.actions.value.items.head.href mustBe expectedUrl
      result(1).actions.value.items.head.href mustBe expectedUrl
    }

    "must return onw rows when only agent's full name exists" in {
      val agentsDetails = AgentDetails(fullName = "Name", clientReference = None)
      val userAnswers   = emptyUserAnswers.set(AgentDetailsPage, agentsDetails).success.value

      val result =
        AgentDetailsSummary.rows(userAnswers)

      result must have size 1

      result.head.value.content.toString must include("Name")

      val expectedUrl =
        controllers.cwf1.routes.AgentDetailsController
          .onPageLoad(userAnswersId, CheckMode)
          .url

      result.head.actions.value.items.head.href mustBe expectedUrl
    }
  }
}
