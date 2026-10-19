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

package viewmodels.checkAnswers.sa1

import base.SpecBase
import models.CheckMode
import models.sa1.Name
import play.api.Application
import play.api.test.Helpers.running

class NameSummarySpec extends SpecBase {

  "NameSummary.row" - {

    "must show the full name as the value" in new Setup {
      running(app) {
        row(Name("Jane", "Doe")).value.content.asHtml.toString must include("Jane Doe")
      }
    }

    "must link to the name page in check mode for the journey" in new Setup {
      running(app) {
        row(Name("Jane", "Doe")).actions.value.items.head.href mustEqual
          controllers.sa1.routes.NameController.onPageLoad(userAnswersId, CheckMode).url
      }
    }

    "must escape the name so it is displayed as text" in new Setup {
      running(app) {
        row(Name("<b>Jane</b>", "Doe")).value.content.asHtml.toString must include("&lt;b&gt;Jane&lt;/b&gt;")
      }
    }
  }

  trait Setup {
    lazy val app: Application = applicationBuilder().build()
    lazy val msgs             = messages(app)

    def row(name: Name) = NameSummary.row(name, userAnswersId, CheckMode)(msgs)
  }
}
