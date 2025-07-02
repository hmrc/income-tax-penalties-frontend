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

package uk.gov.hmrc.incometaxpenaltiesfrontend.models.penaltyDetails

import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json._

case class PenaltySuccessResponse(processingDate: String,
                                  penaltyDetails: Option[PenaltyDetails]
                                 )

object PenaltySuccessResponse {

  implicit val getPenaltyDetailsReads: Reads[PenaltySuccessResponse] = (
    (JsPath \ "success" \ "processingDate").read[String] and
      (JsPath \ "success" \ "penaltyData").readNullable[PenaltyDetails]
    )(PenaltySuccessResponse.apply _)

  implicit val writes: Writes[PenaltySuccessResponse] = Writes { pd =>
    Json.obj(
      "success" -> Json.obj(
        "processingDate" -> pd.processingDate,
        "penaltyData" -> Json.toJson(pd.penaltyDetails)
      )
    )
  }

  implicit val format: Format[PenaltySuccessResponse] =
    Format(getPenaltyDetailsReads, writes)
}
