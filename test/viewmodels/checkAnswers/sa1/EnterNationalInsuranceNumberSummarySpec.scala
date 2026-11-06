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

package viewmodels.checkAnswers.sa1

import base.SpecBase
import models.CheckMode
import pages.sa1.{DoYouHaveANinoPage, EnterNationalInsuranceNumberPage}
import play.api.test.Helpers.stubMessages

class EnterNationalInsuranceNumberSummarySpec extends SpecBase {

  private val msgs = stubMessages()

  "EnterNationalInsuranceNumberSummary" - {

    "must return no row when the user does not have a NINO" in {
      val result =
        EnterNationalInsuranceNumberSummary.row(emptyUserAnswers)(msgs)

      result mustBe None
    }

    "must return one row when enterNationalInsuranceNumber answers exist" in {
      val enterNationalInsuranceNumber = "NM772208B"

      val userAnswers =
        emptyUserAnswers
          .set(DoYouHaveANinoPage, true)
          .success
          .value
          .set(EnterNationalInsuranceNumberPage, enterNationalInsuranceNumber)
          .success
          .value

      val result =
        EnterNationalInsuranceNumberSummary.row(userAnswers)(msgs)

      result mustBe defined
      result.value.value.content.toString must include(enterNationalInsuranceNumber)

      val expectedUrl =
        controllers.sa1.routes.EnterNationalInsuranceNumberController
          .onPageLoad(userAnswersId, CheckMode)
          .url

      result.value.actions.value.items.head.href mustBe expectedUrl
    }

    "must show not provided with an Add action when the user has a NINO but has not entered it" in {
      val userAnswers =
        emptyUserAnswers
          .set(DoYouHaveANinoPage, true)
          .success
          .value

      val result =
        EnterNationalInsuranceNumberSummary.row(userAnswers)(msgs)

      result mustBe defined
      result.value.value.content.toString                    must include(msgs("site.notProvided"))
      result.value.actions.value.items.head.content.toString must include(msgs("site.add"))
    }
  }
}
