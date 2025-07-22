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

package uk.gov.hmrc.incometaxpenaltiesfrontend.models.audit

import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.incometaxpenaltiesfrontend.controllers.auth.models.CurrentUserRequest
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.PenaltyType
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.PenaltyType.PenaltyType
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.penaltyDetails.lpp.{LPPDetails, LPPPenaltyCategoryEnum}

case class UserCalculationInfoAuditModel(penaltyNumber: String,
                                         penaltyType: PenaltyType,
                                         penaltyTotalCost: BigDecimal,
                                         penaltyTotalPaid: BigDecimal)(implicit user: CurrentUserRequest[_]) extends AuditModel {

  def this(lppDetails: LPPDetails)(implicit user: CurrentUserRequest[_]) = this(
    penaltyNumber = lppDetails.penaltyChargeReference.getOrElse("-"),
    penaltyType = if(lppDetails.penaltyCategory == LPPPenaltyCategoryEnum.LPP2) {
      PenaltyType.LPP2
    } else {
      PenaltyType.LPP1
    },
    penaltyTotalCost = lppDetails.amountDue,
    penaltyTotalPaid = lppDetails.penaltyAmountPaid.getOrElse(0)
  )

  override val auditType: String = "UserCalculationInfo"
  override val detail: JsValue = user.auditJson ++ Json.obj(
    "penaltyNumber" -> penaltyNumber,
    "penaltyType" -> Json.toJson(penaltyType),
    "penaltyTotalCost" -> penaltyTotalCost,
    "penaltyTotalPaid" -> penaltyTotalPaid,
    "penaltyLeftToPay" -> (penaltyTotalCost - penaltyTotalPaid)
  )
}
