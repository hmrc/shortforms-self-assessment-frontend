package pages.cwf1

import pages.QuestionPage
import play.api.libs.json.JsPath

case object EnterPreviousLastNamePage extends QuestionPage[String] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "enterPreviousLastName"
}
