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
import pages.cwf1.ContactDetailsPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

object ContactDetailsSummary {

  def rows(answers: UserAnswers)(implicit messages: Messages): Seq[SummaryListRow] =
    answers.get(ContactDetailsPage).toSeq.flatMap { answer =>
      Seq(
        SummaryListRowViewModel(
          key = "cwf1.contactDetails.emailAddress.checkYourAnswersLabel",
          value = ValueViewModel(HtmlContent(answer.emailAddress)),
          actions = Seq(
            ActionItemViewModel(
              "site.change",
              controllers.cwf1.routes.ContactDetailsController.onPageLoad(answers.id, CheckMode).url
            )
              .withVisuallyHiddenText(messages("cwf1.contactDetails.emailAddress.change.hidden"))
              .withAttribute("id" -> "contact-details-email-address")
          )
        ),
        SummaryListRowViewModel(
          key = "cwf1.contactDetails.phoneNumber.checkYourAnswersLabel",
          value = ValueViewModel(HtmlContent(answer.phoneNumber)),
          actions = Seq(
            ActionItemViewModel(
              "site.change",
              controllers.cwf1.routes.ContactDetailsController.onPageLoad(answers.id, CheckMode).url
            )
              .withVisuallyHiddenText(messages("cwf1.contactDetails.phoneNumber.change.hidden"))
              .withAttribute("id" -> "contact-details-phone-number")
          )
        )
      )
    }
}
