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
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.CurrentUserRequest
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.ITSAStatus.ITSAStatus
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.compliance.ObligationDetail

case class UserComplianceInfoAuditModel(mandationStatus: ITSAStatus,
                                        complianceWindow: String,
                                        isPenaltyLate: Boolean,
                                        complianceData: Seq[ObligationDetail])(implicit user: CurrentUserRequest[_]) extends AuditModel {

  override val auditType: String = "UserComplianceInfo"
  override val detail: JsValue = user.auditJson ++ Json.obj(
    "mandationStatus" -> mandationStatus,
    "complianceWindow" -> complianceWindow,
    "isPenaltyLate" -> isPenaltyLate,
    "complianceData" -> complianceData
  )
}
