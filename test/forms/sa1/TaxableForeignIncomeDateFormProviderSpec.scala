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

package forms.sa1

import org.scalatest.matchers.must.Matchers.mustBe
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages

import java.time.LocalDate

class TaxableForeignIncomeDateFormProviderSpec extends AnyWordSpec with Matchers {

  private implicit val messages: Messages = stubMessages()
  private val form                        = new TaxableForeignIncomeDateFormProvider()()
  private val fieldName                   = "value"

  private def bind(day: String, month: String, year: String) =
    form.bind(Map(s"$fieldName.day" -> day, s"$fieldName.month" -> month, s"$fieldName.year" -> year))

  "TaxableForeignIncomeDateFormProvider" should {

    "bind a valid date" in {
      val result = bind("19", "3", "2022")
      result.errors mustBe empty
      result.value mustBe Some(LocalDate.of(2022, 3, 19))
    }

    "accept the lower boundary date of 3 January 1753" in {
      bind("3", "1", "1753").errors mustBe empty
    }

    "reject a date with no fields" in {
      bind("", "", "").errors.map(_.message) mustBe Seq("sa1.taxableForeignIncomeDate.error.required.all")
    }

    "reject a date missing the day" in {
      val error = bind("", "3", "2022").errors.head
      error.message mustBe "sa1.taxableForeignIncomeDate.error.required"
      error.args mustBe Seq(messages("date.error.day"))
    }

    "reject a date missing the month and year" in {
      val error = bind("19", "", "").errors.head
      error.message mustBe "sa1.taxableForeignIncomeDate.error.required.two"
      error.args mustBe Seq(messages("date.error.month"), messages("date.error.year"))
    }

    "reject a non-existent date" in {
      bind("31", "2", "2022").errors.map(_.message) mustBe Seq("sa1.taxableForeignIncomeDate.error.invalid")
    }

    "reject a date before 3 January 1753" in {
      bind("2", "1", "1753").errors.map(_.message) mustBe Seq("sa1.taxableForeignIncomeDate.error.mustBeAfterMinimum")
    }
  }
}
