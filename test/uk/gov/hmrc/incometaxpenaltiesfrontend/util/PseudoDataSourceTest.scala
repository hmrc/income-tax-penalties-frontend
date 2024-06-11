package uk.gov.hmrc.incometaxpenaltiesfrontend.util

import org.scalatest.funsuite.AnyFunSuiteLike
import org.scalatest.matchers.should.Matchers
import play.api.libs.json.{JsDefined, JsString, Json}
import uk.gov.hmrc.incometaxpenaltiesfrontend.util.JsPathUtil.JsPathEx

class PseudoDataSourceTest extends AnyFunSuiteLike with Matchers {

  private val aSubmission = Json.parse(PseudoDataSource.submission)

  test("testNino") {
    val result = (aSubmission \ "notification" \ "file" \ "properties").find(_.\("name")===JsDefined(JsString("nino"))) \ 0 \ "value"
    result shouldBe JsDefined(JsString("UX385055C"))
  }

  test("testAppealId") {
    val result = (aSubmission \ "notification" \ "file" \ "properties").find(_.\("name")===JsDefined(JsString("appealId"))) \ 0 \ "value"
    result shouldBe JsDefined(JsString("59537A702589C2BC"))
  }

}
