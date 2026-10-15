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

import models.sa1.Sa1Name
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.all.*

object Sa1NameSummary {

  def row(name: Sa1Name, journeyId: String)(implicit messages: Messages): SummaryListRow =
    SummaryListRowViewModel(
      key = "sa1.name.checkYourAnswersLabel",
      value = ValueViewModel(Text(s"${name.firstName} ${name.lastName}")),
      actions = Seq(
        ActionItemViewModel("site.change", controllers.sa1.routes.Sa1NameController.onPageLoad(journeyId).url)
          .withVisuallyHiddenText(messages("sa1.name.change.hidden"))
      )
    )
}
