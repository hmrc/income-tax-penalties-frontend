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

import uk.gov.hmrc.incometaxpenaltiesfrontend.models.penaltyDetails.lpp.{LPPDetails, LPPPenaltyStatusEnum}
import uk.gov.hmrc.incometaxpenaltiesfrontend.utils.{CurrencyFormatter, TimeMachine}

case class FirstLatePaymentPenaltyCalculationData(penaltyAmount: BigDecimal,
                                                 taxPeriodStartDate: String,
                                                 taxPeriodEndDate: String,
                                                 isPenaltyPaid: Boolean,
                                                 incomeTaxIsPaid: Boolean,
                                                  isEstimate: Boolean,
                                                  isPenaltyOverdue: Boolean,
                                                  penaltyChargeReference: Option[String],
                                                  llpLRCharge: LLPCharge,
                                                  llpHRCharge: Option[LLPCharge]
                                                 ) {
  def this(lppDetails: LPPDetails)(implicit timeMachine: TimeMachine) = this(
    penaltyAmount = lppDetails.amountDue,
    taxPeriodStartDate = lppDetails.principalChargeBillingFrom.toString,
    taxPeriodEndDate = lppDetails.principalChargeBillingTo.toString,
    isPenaltyPaid = lppDetails.isPaid,
    incomeTaxIsPaid = lppDetails.incomeTaxIsPaid,
    isEstimate = lppDetails.penaltyStatus == LPPPenaltyStatusEnum.Accruing,
    isPenaltyOverdue = lppDetails.penaltyChargeDueDate.fold(false)(_.isAfter(timeMachine.getCurrentDate)),
    penaltyChargeReference = lppDetails.penaltyChargeReference,
    llpLRCharge = LLPCharge(
      chargeAmount = lppDetails.lpp1LRCalculationAmt.getOrElse(0),
      daysOverdue = lppDetails.lpp1LRDays.getOrElse("15"),
      penaltyPercentage = lppDetails.lpp1LRPercentage.getOrElse(0.02)
    ),
    llpHRCharge = lppDetails.lpp1HRCalculationAmt.map(calcAmount =>
    LLPCharge(
      chargeAmount = calcAmount,
      daysOverdue = lppDetails.lpp1HRDays.getOrElse("31"),
      penaltyPercentage = lppDetails.lpp1HRPercentage.getOrElse(0.02)
    ))
  )

  val formattedPenaltyAmount = CurrencyFormatter.parseBigDecimalNoPaddedZeroToFriendlyValue(penaltyAmount)
}

