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

package uk.gov.hmrc.incometaxpenaltiesfrontend.models

import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.breathingSpace.BreathingSpace
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.lpp.LatePaymentPenalty
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.lsp.LateSubmissionPenalty


case class PenaltyDetails(totalisations: Option[Totalisations],
                          lateSubmissionPenalty: Option[LateSubmissionPenalty],
                          latePaymentPenalty: Option[LatePaymentPenalty],
                          breathingSpace: Option[Seq[BreathingSpace]]) {

  val lspPointsActive: Int = lateSubmissionPenalty.map(_.summary.activePenaltyPoints).getOrElse(0)
  val lspThreshold: Int = lateSubmissionPenalty.map(_.summary.regimeThreshold).getOrElse(0)

  val totalInterest: BigDecimal =
    totalisations.flatMap(_.totalAccountAccruingInterest).getOrElse(BigDecimal(0)) +
      totalisations.flatMap(_.totalAccountPostedInterest).getOrElse(BigDecimal(0))

  val unpaidIncomeTax: BigDecimal = totalisations.flatMap(_.totalAccountOverdue).getOrElse(BigDecimal(0))

  val countLPPNotPaidOrAppealed: Int =
    latePaymentPenalty.map(_.withoutAppealedPenalties.count(_.penaltyAmountOutstanding.exists(_ > BigDecimal(0)))).getOrElse(0)

  val countLSPNotPaidOrAppealed: Int =
    lateSubmissionPenalty.map(_.withoutAppealedPenalties.count(_.chargeOutstandingAmount.exists(_ > BigDecimal(0)))).getOrElse(0)

  val hasFinancialChargeToPay: Boolean =
    (unpaidIncomeTax + totalInterest + countLPPNotPaidOrAppealed + countLSPNotPaidOrAppealed) > 0

  val lsp = lateSubmissionPenalty.map(_.details).getOrElse(Seq.empty)
  val lpp = latePaymentPenalty.map(_.details).map(_.sorted).getOrElse(Seq.empty)
}

object PenaltyDetails {
  implicit val format: OFormat[PenaltyDetails] = Json.format[PenaltyDetails]
}
