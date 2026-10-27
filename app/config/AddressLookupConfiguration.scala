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

import play.api.i18n.{Lang, MessagesApi}
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{Call, RequestHeader}
import play.twirl.api.HtmlFormat

import javax.inject.{Inject, Singleton}

@Singleton
class AddressLookupConfiguration @Inject() (appConfig: FrontendAppConfig, messagesApi: MessagesApi) {

  def apply(callback: Call, landingPage: Call)(implicit request: RequestHeader): JsObject = {
    def appLabels(language: String): JsObject = Json.obj(
      "appLevelLabels" -> Json.obj("navTitle" -> messagesApi("service.name")(Lang(language)))
    )

    def labels(language: String): JsObject = {
      def message(key: String): String = messagesApi(s"sa1.address.$key")(Lang(language))

      def visuallyHidden(text: String): String =
        s"""<span class="govuk-visually-hidden">${HtmlFormat.escape(text)}</span>"""

      def pageLabels(page: String): JsObject = Json.obj(
        "title"       -> message(s"$page.title"),
        "heading"     -> message(s"$page.title"),
        "submitLabel" -> messagesApi("site.continue")(Lang(language))
      )

      val lookupLabels  = pageLabels("lookup") ++ Json.obj(
        "filterLabel"           -> message("lookup.filter"),
        "postcodeLabel"         -> message("postcode"),
        "manualAddressLinkText" -> message("enterManually")
      )
      val selectLabels  = pageLabels("select") ++ Json.obj("editAddressLinkText" -> message("enterManually"))
      val editLabels    = pageLabels("edit") ++ Json.obj(
        "line1Label"    -> message("line1"),
        "line2Label"    -> message("line2"),
        "line3Label"    -> message("line3"),
        "townLabel"     -> message("town"),
        "postcodeLabel" -> message("postcode"),
        "countryLabel"  -> message("country")
      )
      val confirmLabels = pageLabels("confirm") ++ Json.obj("changeLinkText" -> message("editManually"))
      appLabels(language) ++ Json.obj(
        "countryPickerLabels" -> (pageLabels("countryPicker") ++ Json.obj(
          "countryLabel" -> visuallyHidden(message("countryPicker.label"))
        )),
        "lookupPageLabels"    -> lookupLabels,
        "selectPageLabels"    -> selectLabels,
        "editPageLabels"      -> editLabels,
        "confirmPageLabels"   -> confirmLabels,
        "international"       -> Json.obj(
          "lookupPageLabels"  -> (pageLabels("lookup") ++ Json.obj("manualAddressLinkText" -> message("enterManually"))),
          "selectPageLabels"  -> selectLabels,
          "editPageLabels"    -> editLabels,
          "confirmPageLabels" -> confirmLabels
        )
      )
    }

    Json.obj(
      "version" -> 2,
      "options" -> Json.obj(
        "continueUrl"              -> s"${appConfig.host}${callback.url}",
        "serviceHref"              -> s"${appConfig.host}${landingPage.url}",
        "signOutHref"              -> s"${appConfig.host}${controllers.auth.routes.AuthController.signOut().url}",
        "phaseFeedbackLink"        -> appConfig.feedbackUrl,
        "deskProServiceName"       -> appConfig.appName,
        "showPhaseBanner"          -> false,
        "includeHMRCBranding"      -> false,
        "showBackButtons"          -> true,
        "disableTranslations"      -> true,
        "ukMode"                   -> false,
        "pageHeadingStyle"         -> "govuk-heading-l",
        "confirmPageConfig"        -> Json.obj(
          "showChangeLink"        -> true,
          "showSearchAgainLink"   -> false,
          "showSubHeadingAndInfo" -> false,
          "showConfirmChangeText" -> false
        ),
        "selectPageConfig"         -> Json.obj("showSearchAgainLink" -> false, "showNoneOfTheseOption" -> false),
        "manualAddressEntryConfig" -> Json.obj(
          "showOrganisationName"   -> false,
          "line1MaxLength"         -> 35,
          "line2MaxLength"         -> 35,
          "line3MaxLength"         -> 35,
          "townMaxLength"          -> 35,
          "maxLengthErrorMessages" -> Json.obj(
            "en" -> Json.obj(
              "addressLine1" -> messagesApi("sa1.address.line1.error.maxLength")(Lang("en")),
              "addressLine2" -> messagesApi("sa1.address.line2.error.maxLength")(Lang("en")),
              "addressLine3" -> messagesApi("sa1.address.line3.error.maxLength")(Lang("en")),
              "town"         -> messagesApi("sa1.address.town.error.maxLength")(Lang("en"))
            ),
            "cy" -> Json.obj(
              "addressLine1" -> messagesApi("sa1.address.line1.error.maxLength")(Lang("cy")),
              "addressLine2" -> messagesApi("sa1.address.line2.error.maxLength")(Lang("cy")),
              "addressLine3" -> messagesApi("sa1.address.line3.error.maxLength")(Lang("cy")),
              "town"         -> messagesApi("sa1.address.town.error.maxLength")(Lang("cy"))
            )
          ),
          "mandatoryFields"        -> Json.obj(
            "addressLine1" -> true,
            "addressLine2" -> false,
            "addressLine3" -> false,
            "town"         -> true,
            "postcode"     -> true
          )
        ),
        "timeoutConfig"            -> Json.obj(
          "timeoutAmount" -> appConfig.timeout,
          "timeoutUrl"    -> s"${appConfig.host}${controllers.auth.routes.AuthController.signOutNoSurvey().url}"
        )
      ),
      "labels"  -> Json.obj("en" -> labels("en"))
    )
  }
}
