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

import models.sa1.WhyTaxReturn
import models.{CheckMode, UserAnswers}
import pages.sa1.WhyTaxReturnPage
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

object WhyTaxReturnSummary {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(WhyTaxReturnPage).map { reasons =>

      // Preserve the display order of the checkboxes rather than Set iteration order
      val value = WhyTaxReturn.values
        .filter(reasons.contains)
        .map(reason => HtmlFormat.escape(messages(s"sa1.whyTaxReturn.$reason")).toString)
        .mkString("<br>")

      SummaryListRowViewModel(
        key = "sa1.whyTaxReturn.checkYourAnswersLabel",
        value = ValueViewModel(HtmlContent(value)),
        actions = Seq(
          ActionItemViewModel(
            "site.change",
            controllers.sa1.routes.WhyTaxReturnController.onPageLoad(answers.id, CheckMode).url
          )
            .withVisuallyHiddenText(messages("sa1.whyTaxReturn.change.hidden"))
        )
      )
    }
}
