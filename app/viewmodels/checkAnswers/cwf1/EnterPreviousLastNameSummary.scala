package viewmodels.checkAnswers.cwf1

import controllers.routes
import models.{CheckMode, UserAnswers}
import pages.cwf1.EnterPreviousLastNamePage
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

object EnterPreviousLastNameSummary  {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(EnterPreviousLastNamePage).map {
      answer =>

        SummaryListRowViewModel(
          key     = "enterPreviousLastName.checkYourAnswersLabel",
          value   = ValueViewModel(HtmlFormat.escape(answer).toString),
          actions = Seq(
            ActionItemViewModel("site.change", routes.EnterPreviousLastNameController.onPageLoad(CheckMode).url)
              .withVisuallyHiddenText(messages("enterPreviousLastName.change.hidden"))
          )
        )
    }
}
