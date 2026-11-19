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

package viewmodels.checkAnswers.cwf1

import controllers.routes
import models.{CheckMode, UserAnswers}
import pages.cwf1.EnterPreviousLastNamePage
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

object EnterPreviousLastNameSummary {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(EnterPreviousLastNamePage).map { answer =>
      SummaryListRowViewModel(
        key = "cwf1.enterPreviousLastName.checkYourAnswersLabel",
        value = ValueViewModel(HtmlFormat.escape(answer).toString),
        actions = Seq(
          ActionItemViewModel(
            "site.change",
            controllers.cwf1.routes.EnterPreviousLastNameController.onPageLoad(answers.id, CheckMode).url
          )
            .withVisuallyHiddenText(messages("cwf1.enterPreviousLastName.change.hidden"))
            .withAttribute("id" -> "enter-previous-last-name")
        )
      )
    }
}
