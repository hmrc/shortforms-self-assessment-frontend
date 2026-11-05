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

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.test.Helpers.stubMessages

class DeclarationFormProviderSpec extends AnyWordSpec with Matchers {

  stubMessages()
  private val form = new DeclarationFormProvider()()

  "DeclarationFormProvider" should {

    "accept an empty name" in {
      form.bind(Map("value" -> "")).errors mustBe empty
    }

    "accept a name of 100 characters" in {
      form.bind(Map("value" -> "A" * 100)).errors mustBe empty
    }

    "reject a name longer than 100 characters" in {
      form.bind(Map("value" -> "A" * 101)).errors.map(_.message) mustBe
        Seq("sa1.declaration.name.error.length")
    }
  }
}
