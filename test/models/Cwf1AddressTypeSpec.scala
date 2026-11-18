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

package models

import base.SpecBase
import play.api.mvc.PathBindable

class Cwf1AddressTypeSpec extends SpecBase {

  private val binder = summon[PathBindable[Cwf1AddressType]]

  "Cwf1AddressType path binder" - {
    Seq(
      Cwf1AddressType.Agent        -> "agent",
      Cwf1AddressType.Home         -> "home",
      Cwf1AddressType.PreviousHome -> "previous-home"
    ).foreach { (addressType, segment) =>
      s"must round-trip $addressType as '$segment'" in {
        binder.unbind("addressType", addressType) mustBe segment
        binder.bind("addressType", segment) mustBe Right(addressType)
      }
    }

    "must reject an unknown address type" in {
      binder.bind("addressType", "Agent").isLeft mustBe true
    }
  }
}
