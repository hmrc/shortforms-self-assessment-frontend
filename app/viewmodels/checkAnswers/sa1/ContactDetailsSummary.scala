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
import pages.sa1.ContactDetailsPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

object ContactDetailsSummary  {

  def rows(journeyId: String, answers: UserAnswers)(implicit messages: Messages): Seq[SummaryListRow] =

    answers.get(ContactDetailsPage).toSeq.flatMap {
      answer =>
      Seq(
        SummaryListRowViewModel(
          key     = "contactDetails.checkYourAnswersLabel",
          value   = ValueViewModel(HtmlContent(answer.emailAddress)),
          actions = Seq(changeAction(journeyId,"sa1.ontactDetails.change.hidden"))
          ),
        SummaryListRowViewModel(
          key = "contactDetails.checkYourAnswersLabel",
          value = ValueViewModel(HtmlContent(answer.phoneNumber)),
          actions = Seq(changeAction(journeyId,"sa1.ontactDetails.change.hidden"))
          )
        )
    }
  private def changeAction(journeyId: String, hiddenTextKey: String)(implicit messages: Messages) =
      ActionItemViewModel(
        "site.change",
        controllers.sa1.routes.ContactDetailsController.onPageLoad(journeyId, CheckMode).url
      ).withVisuallyHiddenText(messages(hiddenTextKey))
}
