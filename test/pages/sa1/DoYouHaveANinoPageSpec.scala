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

package pages.sa1

import base.SpecBase

class DoYouHaveANinoPageSpec extends SpecBase {

  "DoYouHaveANinoPage" - {

    "cleanup" - {

      "must remove EnterNationalInsuranceNumberPage when No is selected" in {

        val userAnswers = emptyUserAnswers
          .set(EnterNationalInsuranceNumberPage, "AB123456C")
          .success
          .value

        val updatedUserAnswers = userAnswers
          .set(DoYouHaveANinoPage, false)
          .success
          .value

        updatedUserAnswers.get(EnterNationalInsuranceNumberPage) mustBe None
      }

      "must remove NoNinoReasonPage when Yes is selected" in {

        val userAnswers = emptyUserAnswers
          .set(NoNinoReasonPage, "I do not have a NINO")
          .success
          .value

        val updatedUserAnswers = userAnswers
          .set(DoYouHaveANinoPage, true)
          .success
          .value

        updatedUserAnswers.get(NoNinoReasonPage) mustBe None
      }

      "must not remove either answer when no value is provided" in {

        val userAnswers = emptyUserAnswers
          .set(EnterNationalInsuranceNumberPage, "AB123456C")
          .success
          .value

        val updatedUserAnswers = DoYouHaveANinoPage
          .cleanup(None, userAnswers)
          .success
          .value

        updatedUserAnswers.get(EnterNationalInsuranceNumberPage) mustBe Some("AB123456C")
      }
    }
  }
}
