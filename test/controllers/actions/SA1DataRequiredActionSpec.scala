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

package controllers.actions

import base.SpecBase
import models.JourneyType
import models.requests.{DataRequest, IdentifierRequest}
import org.mockito.ArgumentMatchers.eq as eqTo
import org.mockito.Mockito.{mock, verify, when}
import play.api.mvc.ActionRefiner

class SA1DataRequiredActionSpec extends SpecBase {

  "SA1DataRequiredAction" - {

    "must delegate to DataRequiredAction with the journey ID and SA1 journey type" in {
      val journeyId = "test-journey-id"

      val dataRequiredAction = mock(classOf[DataRequiredAction])

      val expectedAction =
        mock(classOf[ActionRefiner[IdentifierRequest, DataRequest]])

      when(dataRequiredAction(eqTo(journeyId), eqTo(JourneyType.SA1)))
        .thenReturn(expectedAction)

      val action = new SA1DataRequiredAction(dataRequiredAction)

      val result = action(journeyId)

      result mustBe expectedAction

      verify(dataRequiredAction)(journeyId, JourneyType.SA1)
    }
  }
}
