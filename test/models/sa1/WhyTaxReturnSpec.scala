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

package models.sa1

import base.SpecBase
import play.api.libs.json.{JsError, JsString, Json}

class WhyTaxReturnSpec extends SpecBase {

  "WhyTaxReturn" - {

    "must deserialise valid values" in {
      WhyTaxReturn.values.foreach { value =>
        JsString(value.toString).validate[WhyTaxReturn].asOpt.value mustEqual value
      }
    }

    "must fail to deserialise invalid values" in {
      JsString("invalid value").validate[WhyTaxReturn] mustEqual JsError("error.invalid")
    }

    "must serialise" in {
      WhyTaxReturn.values.foreach { value =>
        Json.toJson(value) mustEqual JsString(value.toString)
      }
    }

    "must round-trip a set of values through JSON" in {
      val reasons: Set[WhyTaxReturn] = Set(WhyTaxReturn.CompanyDirector, WhyTaxReturn.Other)

      Json.toJson(reasons).as[Set[WhyTaxReturn]] mustEqual reasons
    }

    "must list the options in the order they are displayed" in {
      WhyTaxReturn.values mustEqual Seq(
        WhyTaxReturn.CompanyDirector,
        WhyTaxReturn.UkPropertyIncome,
        WhyTaxReturn.ForeignIncome,
        WhyTaxReturn.TrustIncome,
        WhyTaxReturn.IncomeThresholds,
        WhyTaxReturn.UntaxedIncome,
        WhyTaxReturn.ChildBenefitCharge,
        WhyTaxReturn.CapitalGainsTax,
        WhyTaxReturn.Other
      )
    }
  }
}
