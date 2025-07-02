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

package fixtures

import uk.gov.hmrc.incometaxpenaltiesfrontend.models.penaltyDetails.lpp.{LPPDetailsMetadata, LatePaymentPenalty, MainTransactionEnum}
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.penaltyDetails.lsp.{LSPSummary, LateSubmissionPenalty}
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.penaltyDetails.{PenaltyDetails, Totalisations}

import java.time.LocalDate

trait PenaltiesDetailsTestData extends LSPDetailsTestData with LPPDetailsTestData {

  val sampleDate: LocalDate = LocalDate.of(2021, 1, 1)

  val emptyPenaltyDetailsModel: PenaltyDetails = PenaltyDetails(
    totalisations = None,
    lateSubmissionPenalty = None,
    latePaymentPenalty = None,
    breathingSpace = None
  )

  val lateSubmissionPenalty: LateSubmissionPenalty = LateSubmissionPenalty(
    summary = LSPSummary(
      activePenaltyPoints = 1,
      inactivePenaltyPoints = 0,
      regimeThreshold = 4,
      penaltyChargeAmount = 200,
      pocAchievementDate = Some(LocalDate.of(2022, 1, 1))
    ),
    details = Seq(sampleLateSubmissionPoint)
  )

  val latePaymentPenalty: LatePaymentPenalty = LatePaymentPenalty(Some(Seq(
    sampleUnpaidLPP1.copy(metadata = LPPDetailsMetadata(principalChargeMainTr = MainTransactionEnum.ITSAReturnFirstLPP, timeToPay = None))
  )))

  val latePaymentPenalty2: LatePaymentPenalty = LatePaymentPenalty(Some(Seq(
    sampleLPP2.copy(metadata = LPPDetailsMetadata(principalChargeMainTr = MainTransactionEnum.ITSAReturnSecondLPP, timeToPay = None))
  )))

  val samplePenaltyDetailsModel: PenaltyDetails = PenaltyDetails(
    totalisations = Some(Totalisations(
      lspTotalValue = Some(200),
      penalisedPrincipalTotal = Some(2000),
      lppPostedTotal = Some(165.25),
      lppEstimatedTotal = Some(15.26),
      totalAccountOverdue = None,
      totalAccountPostedInterest = None,
      totalAccountAccruingInterest = None
    )),
    lateSubmissionPenalty = Some(lateSubmissionPenalty),
    latePaymentPenalty = Some(latePaymentPenalty),
    breathingSpace = None
  )

  val samplePenaltyDetailsLPP2Model: PenaltyDetails = PenaltyDetails(
    totalisations = Some(Totalisations(
      lspTotalValue = Some(200),
      penalisedPrincipalTotal = Some(2000),
      lppPostedTotal = Some(165.25),
      lppEstimatedTotal = Some(15.26),
      totalAccountOverdue = None,
      totalAccountPostedInterest = None,
      totalAccountAccruingInterest = None
    )),
    lateSubmissionPenalty = Some(lateSubmissionPenalty),
    latePaymentPenalty = Some(latePaymentPenalty2),
    breathingSpace = None
  )

  val samplePenaltyDetailsModelWithoutMetadata: PenaltyDetails = samplePenaltyDetailsModel.copy(latePaymentPenalty = Some(LatePaymentPenalty(Some(Seq(sampleUnpaidLPP1)))))
}
