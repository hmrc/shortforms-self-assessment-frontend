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

import models.{CheckMode, UserAnswers}
import pages.sa1.PreviousLastNamePage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.all.{ActionItemViewModel, SummaryListRowViewModel, ValueViewModel, stringToKey}
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

object EnterPreviousLastNameSummary {

  def rows(
            journeyId: String,
            answers: UserAnswers
          )(implicit messages: Messages): Seq[SummaryListRow] =
    answers.get(PreviousLastNamePage).toSeq.flatMap { answer =>
      Seq(
        SummaryListRowViewModel(
          key = "sa1.enterPreviousLastName.checkYourAnswersLabel",
          value = ValueViewModel(answer.previousLastName),
          actions = Seq(
            ActionItemViewModel(
              "site.change",
              controllers.sa1.routes.EnterPreviousLastNameController
                .onPageLoad(journeyId, CheckMode).url
            ).withVisuallyHiddenText(
              messages("sa1.sa1AddPreviousLastName.change.hidden")
            )
          )
        ),
        SummaryListRowViewModel(
          key = "sa1.enterPreviousLastName.change.hidden",
          value = ValueViewModel(
            answer.lastNameChangeDate.format(
              java.time.format.DateTimeFormatter.ofPattern("d MMMM yyyy")
            )
          ),
          actions = Seq(
            ActionItemViewModel(
              "site.change",
              controllers.sa1.routes.EnterPreviousLastNameController.onPageLoad(journeyId, CheckMode).url
            ).withVisuallyHiddenText(
              messages("sa1.enterPreviousLastName.date.change.hidden")
            )
          )
        )
      )
    }
}