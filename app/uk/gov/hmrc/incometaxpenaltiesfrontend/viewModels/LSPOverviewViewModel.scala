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

package uk.gov.hmrc.incometaxpenaltiesfrontend.viewModels

import uk.gov.hmrc.incometaxpenaltiesfrontend.models.penaltyDetails.lsp.{LSPTypeEnum, LateSubmissionPenalty}

case class LSPOverviewViewModel(lateSubmission: LateSubmissionPenalty) {

  val activePoints: Int = lateSubmission.summary.activePenaltyPoints
  val threshold: Int = lateSubmission.summary.regimeThreshold

  val pointsTotal: Int =
    if(activePoints >= threshold) threshold else activePoints

  val numberOfFinancialPenalties: Int =
    lateSubmission.withoutAppealedPenalties.count(_.lspTypeEnum == LSPTypeEnum.Financial)

  val addedPoint: Boolean = lateSubmission.details.exists(_.lspTypeEnum == LSPTypeEnum.AddedFAP)
}
