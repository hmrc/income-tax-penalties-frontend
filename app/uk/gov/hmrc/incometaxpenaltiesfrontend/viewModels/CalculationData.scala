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

import java.time.LocalDate

sealed trait CalculationData {
  val penaltyAmount: BigDecimal
  val taxPeriodStartDate: LocalDate
  val taxPeriodEndDate: LocalDate
  val isPenaltyPaid: Boolean
  val incomeTaxIsPaid: Boolean
  val isEstimate: Boolean
  val isPenaltyOverdue: Boolean
  val penaltyChargeReference: Option[String]
}

case class FirstLatePaymentPenaltyCalculationData(penaltyAmount: BigDecimal,
                                                  taxPeriodStartDate: LocalDate,
                                                  taxPeriodEndDate: LocalDate,
                                                  isPenaltyPaid: Boolean,
                                                  incomeTaxIsPaid: Boolean,
                                                  isEstimate: Boolean,
                                                  isPenaltyOverdue: Boolean,
                                                  payPenaltyBy: LocalDate,
                                                  penaltyChargeReference: Option[String],
                                                  llpLRCharge: LLPCharge,
                                                  llpHRCharge: Option[LLPCharge]
                                                 ) extends CalculationData {
  def this(lppDetails: LPPDetails)(implicit timeMachine: TimeMachine) = this(
    penaltyAmount = lppDetails.amountDue,
    taxPeriodStartDate = lppDetails.principalChargeBillingFrom,
    taxPeriodEndDate = lppDetails.principalChargeBillingTo,
    isPenaltyPaid = lppDetails.isPaid,
    incomeTaxIsPaid = lppDetails.incomeTaxIsPaid,
    isEstimate = lppDetails.penaltyStatus == LPPPenaltyStatusEnum.Accruing,
    payPenaltyBy = lppDetails.penaltyChargeDueDate.getOrElse(timeMachine.getCurrentDate),
    isPenaltyOverdue = lppDetails.penaltyChargeDueDate.exists(_.isBefore(timeMachine.getCurrentDate.plusDays(1))),
    penaltyChargeReference = lppDetails.penaltyChargeReference,
    llpLRCharge = LLPCharge(
      chargeAmount = lppDetails.lpp1LRCalculationAmt.getOrElse(0),
      daysOverdue = lppDetails.lpp1LRDays.getOrElse("15"),
      penaltyPercentage = lppDetails.lpp1LRPercentage.getOrElse(0.03)
    ),
    llpHRCharge = lppDetails.lpp1HRCalculationAmt.map(calcAmount =>
      LLPCharge(
        chargeAmount = calcAmount,
        daysOverdue = lppDetails.lpp1HRDays.getOrElse("31"),
        penaltyPercentage = lppDetails.lpp1HRPercentage.getOrElse(0.03)
      ))
  )

  val formattedPenaltyAmount: String = CurrencyFormatter.parseBigDecimalNoPaddedZeroToFriendlyValue(penaltyAmount)

}

case class SecondLatePaymentPenaltyCalculationData(penaltyAmount: BigDecimal,
                                                   taxPeriodStartDate: LocalDate,
                                                   taxPeriodEndDate: LocalDate,
                                                   isPenaltyPaid: Boolean,
                                                   incomeTaxIsPaid: Boolean,
                                                   isEstimate: Boolean,
                                                   isPenaltyOverdue: Boolean,
                                                   payPenaltyBy: LocalDate,
                                                   penaltyChargeReference: Option[String],
                                                   penaltyPercentage: BigDecimal,
                                                   daysOverdue: String,
                                                   amountPenaltyAppliedTo: BigDecimal,
                                                   chargeStartDate: LocalDate,
                                                   chargeEndDate: LocalDate
                                                  ) extends CalculationData {
  def this(lppDetails: LPPDetails)(implicit timeMachine: TimeMachine) = this(
    penaltyAmount = lppDetails.amountDue,
    taxPeriodStartDate = lppDetails.principalChargeBillingFrom,
    taxPeriodEndDate = lppDetails.principalChargeBillingTo,
    isPenaltyPaid = lppDetails.isPaid,
    incomeTaxIsPaid = lppDetails.incomeTaxIsPaid,
    isEstimate = lppDetails.penaltyStatus == LPPPenaltyStatusEnum.Accruing,
    payPenaltyBy = lppDetails.penaltyChargeDueDate.getOrElse(timeMachine.getCurrentDate),
    isPenaltyOverdue = lppDetails.penaltyChargeDueDate.exists(_.isBefore(timeMachine.getCurrentDate.plusDays(1))),
    penaltyChargeReference = lppDetails.penaltyChargeReference,
    penaltyPercentage = lppDetails.lpp2Percentage.getOrElse(0.04),
    daysOverdue = lppDetails.lpp2Days.getOrElse("31"),
    amountPenaltyAppliedTo = lppDetails.lpp1HRCalculationAmt.get,
    chargeStartDate = lppDetails.penaltyChargeDueDate.get,
    chargeEndDate = lppDetails.communicationsDate.getOrElse(timeMachine.getCurrentDate)
  )

  val formattedPenaltyAmount: String = CurrencyFormatter.parseBigDecimalNoPaddedZeroToFriendlyValue(penaltyAmount)
  val formattedAmountPenaltyAppliedTo: String = CurrencyFormatter.parseBigDecimalNoPaddedZeroToFriendlyValue(amountPenaltyAppliedTo)

}
