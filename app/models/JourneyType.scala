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

package models

import play.api.libs.json._

sealed trait JourneyType {
  def value: String
}

object JourneyType {

  case object SA1 extends JourneyType {
    override val value: String = "SA1"
  }

  case object CeaseTrading extends JourneyType {
    override val value: String = "CeaseTrading"
  }

  case object CWF1ST extends JourneyType {
    override val value: String = "CWF1ST"
  }

  val values: Seq[JourneyType] = Seq(SA1, CeaseTrading, CWF1ST)

  def fromString(value: String): Option[JourneyType] =
    values.find(_.value == value)

  given Format[JourneyType] = Format(
    Reads {
      case JsString(value) =>
        fromString(value).map(JsSuccess(_))
          .getOrElse(JsError(s"Invalid JourneyType: $value"))
      case _ => JsError("JourneyType must be a string")
    },
    Writes(journeyType => JsString(journeyType.value))
  )
}

