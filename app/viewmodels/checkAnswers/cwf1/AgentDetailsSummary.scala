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

import models.{CheckMode, UserAnswers}
import pages.cwf1.AgentDetailsPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

object AgentDetailsSummary {

  def rows(answers: UserAnswers)(implicit messages: Messages): Seq[SummaryListRow] =
    answers.get(AgentDetailsPage).toSeq.flatMap { answer =>

      val fullNameRow = SummaryListRowViewModel(
        key = "cwf1.agentDetails.fullName.checkYourAnswersLabel",
        value = ValueViewModel(HtmlContent(answer.fullName)),
        actions = Seq(
          ActionItemViewModel(
            "site.change",
            controllers.cwf1.routes.AgentDetailsController.onPageLoad(answers.id, CheckMode).url
          )
            .withVisuallyHiddenText(messages("cwf1.agentDetails.fullName.change.hidden"))
        )
      )

      val clientReferenceRow =
        answer.clientReference.map { ref =>
          SummaryListRowViewModel(
            key = "cwf1.agentDetails.clientReference.checkYourAnswersLabel",
            value = ValueViewModel(HtmlContent(ref)),
            actions = Seq(
              ActionItemViewModel(
                "site.change",
                controllers.cwf1.routes.AgentDetailsController
                  .onPageLoad(answers.id, CheckMode)
                  .url
              ).withVisuallyHiddenText(
                messages("cwf1.agentDetails.clientReference.change.hidden")
              )
            )
          )
        }

      Seq(
        fullNameRow
      ) ++ clientReferenceRow.toSeq
    }
}
