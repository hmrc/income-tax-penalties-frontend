/*
 * Copyright 2023 HM Revenue & Customs
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

package uk.gov.hmrc.incometaxpenaltiesfrontend.models.compliance

import play.api.libs.json._

object ComplianceStatusEnum extends Enumeration {
  val Open: ComplianceStatusEnum.Value = Value("O")
  val Fulfilled: ComplianceStatusEnum.Value = Value("F")

  implicit val format: Format[ComplianceStatusEnum.Value] = new Format[ComplianceStatusEnum.Value] {
    override def writes(o: ComplianceStatusEnum.Value): JsValue = {
      JsString(o.toString)
    }

    override def reads(json: JsValue): JsResult[ComplianceStatusEnum.Value] = {
      json.as[String] match {
        case "O" => JsSuccess(Open)
        case "F" => JsSuccess(Fulfilled)
        case e => JsError(s"$e not recognised")
      }
    }
  }

  val auditWrites: Writes[Value] = Writes[ComplianceStatusEnum.Value] {
    case Open => JsString("Open")
    case _ =>    JsString("Fulfilled")
  }

}
