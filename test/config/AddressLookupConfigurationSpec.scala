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

package config

import base.SpecBase
import play.api.Application
import play.api.libs.json.{JsObject, JsValue}
import play.api.test.FakeRequest
import play.api.test.Helpers.running

class AddressLookupConfigurationSpec extends SpecBase {
  "ALF configuration" - {
    "must use absolute callbacks, standard country routing and no custom truncation limits" in new Setup {
      running(app) {
        implicit val request = FakeRequest()
        val callback         = controllers.routes.AddressLookupController.callback(userAnswersId)
        val configuration    = app.injector.instanceOf[AddressLookupConfiguration].apply(callback)
        (configuration \ "version").as[Int] mustBe 2
        val options          = (configuration \ "options").as[JsObject]
        (options \ "continueUrl").as[String] mustBe s"https://example.test${callback.url}"
        (options \ "ukMode").as[Boolean] mustBe false
        (options \ "disableTranslations").as[Boolean] mustBe false
        val manualEntry      = (options \ "manualAddressEntryConfig").as[JsObject]
        Seq("line1MaxLength", "line2MaxLength", "line3MaxLength", "townMaxLength").foreach { field =>
          (manualEntry \ field).toOption mustBe None
        }
        (manualEntry \ "showOrganisationName").as[Boolean] mustBe false
        (manualEntry \ "mandatoryFields").as[JsObject] mustBe play.api.libs.json.Json.obj(
          "addressLine1" -> true,
          "addressLine2" -> false,
          "addressLine3" -> false,
          "town"         -> true,
          "postcode"     -> true
        )
        (options \ "timeoutConfig" \ "timeoutAmount").as[Int] mustBe 900
        (options \ "signOutHref").as[String] mustBe
          s"https://example.test${controllers.auth.routes.AuthController.signOut().url}"
        (configuration \ "labels" \ "en" \ "appLevelLabels" \ "navTitle").as[String] mustBe
          messages(app)("service.name")
        (options \ "showBackButtons").as[Boolean] mustBe true
        (options \ "showPhaseBanner").as[Boolean] mustBe false
        (options \ "includeHMRCBranding").as[Boolean] mustBe false
        (options \ "pageHeadingStyle").as[String] mustBe "govuk-heading-l"
        (options \ "confirmPageConfig").as[JsObject] mustBe play.api.libs.json.Json.obj(
          "showChangeLink"        -> true,
          "showSearchAgainLink"   -> false,
          "showSubHeadingAndInfo" -> false,
          "showConfirmChangeText" -> false
        )
        (options \ "selectPageConfig").as[JsObject] mustBe play.api.libs.json.Json.obj(
          "showSearchAgainLink"   -> false,
          "showNoneOfTheseOption" -> false
        )
      }
    }

    "must configure all five English address screens using supplied wording" in new Setup {
      running(app) {
        implicit val request = FakeRequest()
        val configuration    = app.injector
          .instanceOf[AddressLookupConfiguration]
          .apply(controllers.routes.AddressLookupController.callback(userAnswersId))
        val labels           = (configuration \ "labels" \ "en").as[JsObject]
        val expectedHeadings = Map(
          "countryPickerLabels" -> "What is the country or territory of your address?",
          "lookupPageLabels"    -> "Search for your address",
          "editPageLabels"      -> "What is your address?",
          "selectPageLabels"    -> "Choose your address",
          "confirmPageLabels"   -> "Review your address"
        )
        expectedHeadings.foreach { (page, heading) =>
          (labels \ page \ "title").as[String] mustBe heading
          (labels \ page \ "heading").as[String] mustBe heading
          (labels \ page \ "submitLabel").as[String] mustBe "Continue"
          if (page != "countryPickerLabels") {
            (labels \ "international" \ page \ "heading").as[String] mustBe heading
            (labels \ "international" \ page \ "submitLabel").as[String] mustBe "Continue"
          }
        }
        (labels \ "lookupPageLabels" \ "filterLabel").as[String] mustBe "Property name and number (optional)"
        (labels \ "lookupPageLabels" \ "postcodeLabel").as[String] mustBe "Postcode"
        (labels \ "lookupPageLabels" \ "manualAddressLinkText").as[String] mustBe "Enter your address manually"
        (labels \ "selectPageLabels" \ "editAddressLinkText").as[String] mustBe "Enter your address manually"
        (labels \ "confirmPageLabels" \ "changeLinkText").as[String] mustBe "Edit this address manually"
        (labels \ "international" \ "confirmPageLabels" \ "changeLinkText")
          .as[String] mustBe "Edit this address manually"
        val edit             = (labels \ "editPageLabels").as[JsObject]
        Seq(
          "line1Label"    -> "Address line 1",
          "line2Label"    -> "Address line 2",
          "line3Label"    -> "Address line 3",
          "townLabel"     -> "Town or city",
          "postcodeLabel" -> "Postcode",
          "countryLabel"  -> "Country"
        ).foreach { (key, label) =>
          (edit \ key).as[String] mustBe label
        }
        (labels \ "international" \ "editPageLabels").as[JsObject] mustBe edit
        (labels \ "international" \ "lookupPageLabels" \ "filterLabel").toOption mustBe None
        (labels \ "countryPickerLabels" \ "countryLabel").as[String] mustBe
          """<span class="govuk-visually-hidden">Country or territory</span>"""
      }
    }

    "must respect the translation feature flag" in new SetupWithoutTranslations {
      running(app) {
        implicit val request = FakeRequest()
        val config           = app.injector
          .instanceOf[AddressLookupConfiguration]
          .apply(controllers.routes.AddressLookupController.callback(userAnswersId))
        (config \ "options" \ "disableTranslations").as[Boolean] mustBe true
      }
    }
  }

  trait Setup {
    def translationsEnabled: Boolean = true

    lazy val app: Application = applicationBuilder()
      .configure("host" -> "https://example.test", "features.welsh-translation" -> translationsEnabled)
      .build()
  }

  trait SetupWithoutTranslations extends Setup {
    override def translationsEnabled: Boolean = false
  }
}
