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

import play.api.data.FormError
import play.api.data.format.Formatter
import play.api.libs.json.Format

final class Nino private[models] (val underlying: String) extends AnyVal {

  override def toString: String = underlying

  def ==(that: Nino): Boolean = this.canonicalValue == that.canonicalValue

  def canonicalValue: String = underlying.replace(" ", "").toUpperCase
}

object Nino {
  private val maxLength = 9
  private val pattern   = "^[A-Za-z]{2}[0-9]{6}[A-Za-z ]?$"

  def get(value: String): Either[String, Nino] =
    val stripped = value.replace(" ", "")
    if stripped.length > maxLength then return Left("error.nino.length")
    if !stripped.matches(pattern) then return Left("error.nino.pattern")
    Right(new Nino(value))

  given Formatter[Nino] = new Formatter[Nino] {
    def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Nino] =
      data
        .get(key)
        .toRight("error.nino.required")
        .flatMap(get)
        .left
        .map(error => Seq(FormError(key, error)))

    def unbind(key: String, value: Nino): Map[String, String] = Map(key -> value.underlying)
  }

  given Format[Nino] = Format.of[String].bimap(new Nino(_), _.underlying)
}
