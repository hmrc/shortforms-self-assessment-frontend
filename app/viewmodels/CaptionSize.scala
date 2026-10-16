/*
 * Copyright 2025 HM Revenue & Customs
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

package viewmodels

sealed trait CaptionSize

object CaptionSize {
  case object ExtraLarge extends WithCssClass("govuk-caption-xl hmrc-caption-xl") with CaptionSize
  case object Large      extends WithCssClass("govuk-caption-l hmrc-caption-l") with CaptionSize
  case object Medium     extends WithCssClass("govuk-caption-m hmrc-caption-m") with CaptionSize
  case object Small      extends WithCssClass("govuk-caption-s hmrc-caption-s") with CaptionSize
}
