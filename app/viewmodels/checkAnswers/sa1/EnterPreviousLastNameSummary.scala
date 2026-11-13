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
import pages.sa1.{AddPreviousLastNamePage, PreviousLastNamePage}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

import java.time.format.DateTimeFormatter

object EnterPreviousLastNameSummary {

  private val dateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")

  def rows(
    answers: UserAnswers
  )(implicit messages: Messages): Seq[SummaryListRow] =
    answers.get(AddPreviousLastNamePage) match {
      case Some(true) =>
        val previousName = answers.get(PreviousLastNamePage)

        Seq(
          SummaryListRowViewModel(
            key = "sa1.enterPreviousLastName.checkYourAnswersLabel",
            value = ValueViewModel(
              previousName.map(_.previousLastName).getOrElse(messages("site.notProvided"))
            ),
            actions = Seq(
              action(
                answers.id,
                previousName.isDefined,
                "sa1.enterPreviousLastName.change.hidden",
                "previous-last-name"
              )
            )
          ),
          SummaryListRowViewModel(
            key = "sa1.enterPreviousLastName.date.checkYourAnswersLabel",
            value = ValueViewModel(
              previousName
                .map(_.lastNameChangeDate.format(dateFormatter))
                .getOrElse(messages("site.notProvided"))
            ),
            actions = Seq(
              action(
                answers.id,
                previousName.isDefined,
                "sa1.enterPreviousLastName.date.change.hidden",
                "previous-last-name-date"
              )
            )
          )
        )

      case _ => Seq.empty
    }

  private def action(
    journeyId: String,
    isProvided: Boolean,
    hiddenTextKey: String,
    id: String
  )(implicit messages: Messages) =
    ActionItemViewModel(
      if (isProvided) "site.change" else "site.add",
      controllers.sa1.routes.EnterPreviousLastNameController
        .onPageLoad(journeyId, CheckMode)
        .url
    ).withVisuallyHiddenText(messages(hiddenTextKey))
      .withAttribute("id" -> id)

}
