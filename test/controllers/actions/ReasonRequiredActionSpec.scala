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

package controllers.actions

import base.SpecBase
import models.requests.{DataRequest, IdentifierRequest}
import models.sa1.WhyTaxReturn
import models.{NormalMode, UserAnswers}
import org.scalatest.prop.TableDrivenPropertyChecks
import pages.sa1.WhyTaxReturnPage
import play.api.http.Status
import play.api.mvc.Results

import scala.concurrent.Future
import scala.util.Random

class ReasonRequiredActionSpec extends SpecBase with Results with Status with TableDrivenPropertyChecks {
  import play.api.test.Helpers.*

  private val actionUnderTest = new ReasonRequiredActionImpl

  private val reasons = Table("Reason", WhyTaxReturn.values*)

  private val journeyId                      = Random.nextString(16)
  private val whyTaxReturnRoute              = controllers.sa1.routes.WhyTaxReturnController.onPageLoad(journeyId, NormalMode)
  override val emptyUserAnswers: UserAnswers = super.emptyUserAnswers.copy(id = journeyId)

  "action filter must" - {
    "pass request through WHEN" - {
      "user answers contains given reason" in forAll(reasons) { reason =>
        val givenReasons = Set(reason)
        val userAnswers  = emptyUserAnswers.update(WhyTaxReturnPage, givenReasons)

        val request = fakeDataRequest(userAnswers)
        val result  = invokeHarness(reason, request)

        status(result) mustBe OK
      }
    }
    "redirect to Why Tax Return page WHEN" - {
      "user answers does NOT contain given reason" in forAll(reasons) { reason =>
        val request = fakeDataRequest()
        val result  = invokeHarness(reason, request)

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe whyTaxReturnRoute.url
      }
    }
  }

  private def invokeHarness(reason: WhyTaxReturn, request: DataRequest[?]) =
    actionUnderTest(reason).invokeBlock(request, _ => Future.successful(Ok))

  private def fakeDataRequest(userAnswers: UserAnswers = emptyUserAnswers) =
    new DataRequest(
      new IdentifierRequest(play.api.test.FakeRequest(), journeyId, Some("NM772208B")),
      userAnswers
    )
}
