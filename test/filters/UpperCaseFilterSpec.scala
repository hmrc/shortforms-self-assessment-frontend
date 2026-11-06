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

package filters

import base.SpecBase
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.mvc.*
import play.api.mvc.Results.*
import play.api.test.FakeRequest
import play.api.test.Helpers.*

import scala.concurrent.Future

class UpperCaseFilterSpec extends SpecBase with GuiceOneAppPerSuite {

  val filter: UpperCaseFilter =
    app.injector.instanceOf[UpperCaseFilter]

  "UpperCaseFilter" - {
    "must allow lowercase paths through" in {
      val request = FakeRequest(GET, "/self-assessment-forms/sa1/name/b24ab280-a19f-44da-ade9-33fc81bf0e3d")

      val result = filter(_ => Future.successful(Ok("passed")))(request)

      status(result) mustBe OK
      contentAsString(result) mustBe "passed"
    }

    "redirect uppercase paths to lowercase" in {
      val request = FakeRequest(GET, "/SELF-ASSESSMENT-FORMS/sa1/name/b24ab280-a19f-44da-ade9-33fc81bf0e3d")

      val result = filter(_ => Future.successful(Ok))(request)

      status(result) mustBe MOVED_PERMANENTLY
      redirectLocation(result) mustBe Some("/self-assessment-forms/sa1/name/b24ab280-a19f-44da-ade9-33fc81bf0e3d")
    }

    "preserve query parameters when redirecting" in {
      val request = FakeRequest(GET, "/SELF-ASSESSMENT-FORMS/sa1/name/b24ab280-a19f-44da-ade9-33fc81bf0e3d?query=pAram")

      val result = filter(_ => Future.successful(Ok))(request)

      status(result) mustBe MOVED_PERMANENTLY
      redirectLocation(result) mustBe Some(
        "/self-assessment-forms/sa1/name/b24ab280-a19f-44da-ade9-33fc81bf0e3d?query=pAram"
      )
    }
  }
}
