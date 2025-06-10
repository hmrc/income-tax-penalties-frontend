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

import uk.gov.hmrc.incometaxpenaltiesfrontend.models.lpp.{LPPDetails, LPPPenaltyStatusEnum}
import uk.gov.hmrc.incometaxpenaltiesfrontend.utils.{CurrencyFormatter, TimeMachine}

case class SecondLatePaymentPenaltyCalculationData(penaltyAmount: BigDecimal,
                                                   taxPeriodStartDate: String,
                                                   taxPeriodEndDate: String,
                                                   isPenaltyPaid: Boolean,
                                                   incomeTaxIsPaid: Boolean,
                                                   isEstimate: Boolean,
                                                   isPenaltyOverdue: Boolean,
                                                   penaltyChargeReference: Option[String],
                                                   penaltyPercentage: BigDecimal,
                                                   daysOverdue: String
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
    penaltyPercentage = lppDetails.LPP2Percentage.getOrElse(0.04),
    daysOverdue = lppDetails.LPP2Days.getOrElse("31")
  )

  val formattedPenaltyAmount = CurrencyFormatter.parseBigDecimalNoPaddedZeroToFriendlyValue(penaltyAmount)

}