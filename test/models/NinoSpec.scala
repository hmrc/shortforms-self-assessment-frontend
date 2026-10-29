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
import org.scalatest.{EitherValues, LoneElement}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.Json

class NinoSpec extends SpecBase with EitherValues with LoneElement with ScalaCheckPropertyChecks {
  import NinoSpec.randomNinos

  "Nino should" - {

    "consider another Nino equal when" - {

      "it only differs in case" in {
        val upperNino = new Nino("QQ123456C")
        val lowerNino = new Nino("qq123456c")

        assert(upperNino == lowerNino)
      }

      "it only differs in spaces" in {
        val compactNino = new Nino("QQ123456C")
        val spacedNino  = new Nino(" QQ 12 34 56 C ")

        assert(compactNino == spacedNino)
      }
    }
  }

  "Formatter should" - {
    val formatterUnderTest = Nino.given_Formatter_Nino

    "recover the same Nino value filled in the form" in
      forAll(randomNinos) { originalNino =>
        val formData      = formatterUnderTest.unbind("value", originalNino)
        val recoveredNino = formatterUnderTest.bind("value", formData).value

        recoveredNino mustBe originalNino
      }

    "return an error when" - {

      "nino is missing from form" in {
        val emptyFormData = Map.empty[String, String]

        val errors = formatterUnderTest.bind("value", emptyFormData).left.value

        errors.loneElement.message mustBe "error.nino.required"
      }

      "nino has more than 9 alphanumeric characters" in {
        val invalidFormData = Map("value" -> "QQ1234567C")

        val errors = formatterUnderTest.bind("value", invalidFormData).left.value

        errors.loneElement.message mustBe "error.nino.length"
      }

      "nino does not match expected pattern" in {
        val invalidFormData = Map("value" -> "QQQ12345C")

        val errors = formatterUnderTest.bind("value", invalidFormData).left.value

        errors.loneElement.message mustBe "error.nino.pattern"
      }
    }
  }

  "JSON Format should" - {

    "recover a Nino encoded as JSON" in
      forAll(randomNinos) { originalNino =>
        val json          = Json.toJson(originalNino)
        val recoveredNino = json.validate[Nino].asEither.value

        recoveredNino mustBe originalNino
      }
  }
}

object NinoSpec {
  import org.scalacheck.Gen

  val randomNinos: Gen[Nino] =
    for
      char1    <- Gen.alphaChar
      char2    <- Gen.alphaChar
      digits   <- Gen.stringOfN(6, Gen.numChar)
      lastChar <- Gen.alphaChar
    yield new Nino(char1 +: char2 +: digits :+ lastChar)
}
