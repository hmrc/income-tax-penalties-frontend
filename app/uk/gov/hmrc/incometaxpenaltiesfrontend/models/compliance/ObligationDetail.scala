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

import play.api.libs.json.{Json, OFormat, Writes}

import java.time.LocalDate

case class ObligationDetail(status: ComplianceStatusEnum.Value,
                            inboundCorrespondenceFromDate: LocalDate,
                            inboundCorrespondenceToDate: LocalDate,
                            inboundCorrespondenceDateReceived: Option[LocalDate],
                            inboundCorrespondenceDueDate: LocalDate,
                            periodKey: String)

object ObligationDetail {

  implicit val format: OFormat[ObligationDetail] = Json.format[ObligationDetail]

  val auditWrites: Writes[ObligationDetail] = Writes { model =>
    Json.obj(
      "status" -> Json.toJson(model.status)(ComplianceStatusEnum.auditWrites),
      "inboundCorrespondenceFromDate" -> model.inboundCorrespondenceFromDate,
      "inboundCorrespondenceToDate" -> model.inboundCorrespondenceToDate,
      "inboundCorrespondenceDueDate" -> model.inboundCorrespondenceDueDate,
      "periodKey" -> model.periodKey
    ) ++ model.inboundCorrespondenceDateReceived.fold(Json.obj())(date => Json.obj("inboundCorrespondenceDateReceived" -> date))
  }
}
