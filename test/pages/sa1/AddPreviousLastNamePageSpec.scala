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
import models.sa1.PreviousName
import java.time.LocalDate

class AddPreviousLastNamePageSpec extends SpecBase {

  "AddPreviousLastNamePage" - {

    "cleanup" - {

      "must remove PreviousLastNamePage when false is selected" in {

        val previousLastName = PreviousName(
          previousLastName = "Smith",
          lastNameChangeDate = LocalDate.of(2025, 1, 1)
        )
        val userAnswers      = emptyUserAnswers
          .set(PreviousLastNamePage, previousLastName)
          .success
          .value

        val updatedUserAnswers = userAnswers
          .set(AddPreviousLastNamePage, false)
          .success
          .value

        updatedUserAnswers.get(PreviousLastNamePage) mustBe None
      }

      "must not remove PreviousLastNamePage when true is selected" in {

        val previousLastName = PreviousName(
          previousLastName = "Smith",
          lastNameChangeDate = LocalDate.of(2025, 1, 1)
        )
        val userAnswers      = emptyUserAnswers
          .set(PreviousLastNamePage, previousLastName)
          .success
          .value

        val updatedUserAnswers = userAnswers
          .set(AddPreviousLastNamePage, true)
          .success
          .value

        updatedUserAnswers.get(PreviousLastNamePage) mustBe Some(previousLastName)
      }

      "must not remove PreviousLastNamePage when no value is provided" in {

        val previousLastName = PreviousName(
          previousLastName = "Smith",
          lastNameChangeDate = LocalDate.of(2025, 1, 1)
        )
        val userAnswers      = emptyUserAnswers
          .set(PreviousLastNamePage, previousLastName)
          .success
          .value

        val updatedUserAnswers = AddPreviousLastNamePage
          .cleanup(None, userAnswers)
          .success
          .value

        updatedUserAnswers.get(PreviousLastNamePage) mustBe Some(previousLastName)
      }
    }
  }
}
