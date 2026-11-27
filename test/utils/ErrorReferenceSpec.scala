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

package utils

import base.SpecBase

class ErrorReferenceSpec extends SpecBase {

  "ErrorReference.generate" - {
    "return a 16 character upper case alphanumeric reference" in {
      val reference = ErrorReference.generate()

      reference must fullyMatch regex "[A-Z0-9]{16}"
    }

    "return a different reference each time" in {
      val references = Seq.fill(20)(ErrorReference.generate()).toSet

      references.size mustBe 20
    }
  }
}
