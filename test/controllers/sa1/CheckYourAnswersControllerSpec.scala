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

package controllers.sa1

import base.SpecBase
import controllers.MockAppControllerComponents
import models.address.{AddressDetails, ConfirmedAddress, Country}
import models.sa1.Name
import models.{CheckMode, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{mock, verify, when}
import pages.AddressPage
import pages.sa1.NamePage
import play.api.i18n.Messages
import play.api.mvc.Request
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import views.html.sa1.CheckYourAnswersView

class CheckYourAnswersControllerSpec extends SpecBase {

  "SA1 Check Your Answers Controller" - {

    "must render the view with empty summary lists when nothing has been answered" in new Setup {
      private val result = controller.onPageLoad(userAnswersId)(FakeRequest())

      status(result) mustBe OK
      contentAsString(result) mustBe stubContent

      private val (personalDetails, applicationDetails) = capturedSummaries()

      personalDetails.rows mustBe empty
      applicationDetails.rows mustBe empty
    }

    "must include the escaped full address with its ALF change link" in new SetupWithSavedAddress {
      private val result = controller.onPageLoad(userAnswersId)(FakeRequest())

      status(result) mustBe OK

      private val (personalDetails, _) = capturedSummaries()
      private val rows                 = personalDetails.rows

      rows must have size 1

      private val addressRow  = rows.head
      private val addressHtml = addressRow.value.content.asInstanceOf[HtmlContent].value.toString

      addressHtml must include("&lt;script&gt;")
      addressHtml must not include "<script>alert"
      addressHtml must include("A very long address line that is not truncated")
      addressHtml must include("Town")
      addressHtml must include("AA1 1AA")
      addressHtml must include("United Kingdom")
      addressHtml must not include "998877665544"

      addressRow.actions.value.items.map(_.href) mustBe
        Seq(routes.AddressLookupController.change(userAnswersId).url)
    }

    "must include the name row before the address row with their change links" in new SetupWithNameAndAddress {
      private val result = controller.onPageLoad(userAnswersId)(FakeRequest())

      status(result) mustBe OK

      private val (personalDetails, _) = capturedSummaries()
      private val rows                 = personalDetails.rows

      rows must have size 3

      rows.head.actions.value.items.map(_.href) mustBe
        Seq(routes.NameController.onPageLoad(userAnswersId, CheckMode).url)

      rows(1).actions.value.items.map(_.href) mustBe
        Seq(routes.NameController.onPageLoad(userAnswersId, CheckMode).url)

      rows(2).actions.value.items.map(_.href) mustBe
        Seq(routes.AddressLookupController.change(userAnswersId).url)
    }

    "must redirect to Check Your Answers when submitted" in new Setup {
      private val result = controller.onSubmit(userAnswersId)(FakeRequest())

      status(result) mustBe SEE_OTHER

      redirectLocation(result) mustBe
        Some(routes.CheckYourAnswersController.onPageLoad(userAnswersId).url)
    }
  }

  trait Setup {
    def userAnswers: UserAnswers           = emptyUserAnswers
    val confirmedAddress: ConfirmedAddress = ConfirmedAddress(
      AddressDetails(
        Seq("<script>alert('address')</script>", "A very long address line that is not truncated"),
        town = Some("Town"),
        postcode = Some("AA1 1AA"),
        country = Some(Country("GB", "United Kingdom"))
      ),
      auditRef = "audit-ref",
      uprn = Some(998877665544L)
    )

    val stubContent: String            = "Check your answers view"
    val stubView: CheckYourAnswersView = mock(classOf[CheckYourAnswersView])

    when(
      stubView.apply(
        any[String],
        any[SummaryList],
        any[SummaryList]
      )(any[Request[?]], any[Messages])
    ).thenReturn(Html(stubContent))

    val controller = new CheckYourAnswersController(
      new MockAppControllerComponents(userAnswers),
      stubView
    )

    def capturedSummaries(): (SummaryList, SummaryList) = {
      val personalDetailsCaptor = ArgumentCaptor.forClass(classOf[SummaryList])

      val applicationDetailsCaptor = ArgumentCaptor.forClass(classOf[SummaryList])

      verify(stubView).apply(
        any[String],
        personalDetailsCaptor.capture(),
        applicationDetailsCaptor.capture()
      )(any[Request[?]], any[Messages])

      (
        personalDetailsCaptor.getValue,
        applicationDetailsCaptor.getValue
      )
    }
  }

  trait SetupWithSavedAddress extends Setup {
    override def userAnswers: UserAnswers = emptyUserAnswers.set(AddressPage, confirmedAddress).success.value
  }

  trait SetupWithNameAndAddress extends Setup {
    override def userAnswers: UserAnswers =
      emptyUserAnswers
        .set(NamePage, Name("Jane", "Doe"))
        .success
        .value
        .set(AddressPage, confirmedAddress)
        .success
        .value
  }
}
