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

package uk.gov.hmrc.incometaxpenaltiesfrontend.views.helpers

import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.penaltyDetails.lpp.{LPPDetails, LPPPenaltyCategoryEnum, LPPPenaltyStatusEnum}
import uk.gov.hmrc.incometaxpenaltiesfrontend.utils.{CurrencyFormatter, DateFormatter, TimeMachine}
import uk.gov.hmrc.incometaxpenaltiesfrontend.viewModels.LatePaymentPenaltySummaryCard

import javax.inject.Inject

class LPPCardHelper @Inject()(lppSummaryRow: LPPSummaryListRowHelper) extends DateFormatter with TagHelper {

  def createLatePaymentPenaltyCards(lpps: Seq[(LPPDetails, Int)], isBreathingSpace: Boolean)(implicit messages: Messages, timeMachine: TimeMachine): Seq[LatePaymentPenaltySummaryCard] =
    lpps.map { case (lpp, index) =>

      val cardRows: Seq[SummaryListRow] =
        lpp.penaltyCategory match {
          case LPPPenaltyCategoryEnum.MANUAL => lppManual(lpp)
          case _ => lppCardBody(lpp)
        }

      val cardTitle = if(lpp.penaltyAmountOutstanding.getOrElse(0) != 0 && lpp.penaltyAmountPaid.getOrElse(0) != 0){
        messages(s"lpp.penaltyType.${lpp.penaltyCategory}.due")
      } else {
        messages(s"lpp.penaltyType.${lpp.penaltyCategory}", CurrencyFormatter.parseBigDecimalNoPaddedZeroToFriendlyValue(lpp.amountDue))
      }

      LatePaymentPenaltySummaryCard(
        index,
        cardTitle = cardTitle,
        cardRows = cardRows,
        status = getTagStatus(lpp, isBreathingSpace),
        penaltyChargeReference = lpp.penaltyChargeReference,
        principalChargeReference = lpp.principalChargeReference,
        isPenaltyPaid = lpp.isPaid,
        amountDue = lpp.amountDue,
        appealStatus = lpp.appealStatus,
        appealLevel = lpp.appealLevel,
        incomeTaxIsPaid = lpp.incomeTaxIsPaid,
        penaltyCategory = lpp.penaltyCategory,
        dueDate = dateToString(lpp.principalChargeDueDate),
        taxPeriodStartDate = lpp.principalChargeBillingFrom.toString,
        taxPeriodEndDate = lpp.principalChargeBillingTo.toString,
        incomeTaxOutstandingAmountInPence = lpp.incomeTaxOutstandingAmountInPence,
        isEstimatedLPP1 = lpp.penaltyCategory == LPPPenaltyCategoryEnum.LPP1 && lpp.penaltyStatus == LPPPenaltyStatusEnum.Accruing
      )
    }

  private def lppCardBody(lpp: LPPDetails)(implicit messages: Messages, timeMachine: TimeMachine): Seq[SummaryListRow] =
    Seq(
      lppSummaryRow.payPenaltyByRow(lpp),
      Some(lppSummaryRow.incomeTaxPeriodRow(lpp)),
      Some(lppSummaryRow.incomeTaxDueRow(lpp)),
      Some(lppSummaryRow.incomeTaxPaymentDateRow(lpp)),
      lppSummaryRow.appealStatusRow(lpp.appealStatus, lpp.appealLevel),
      Some(lppSummaryRow.breathingSpaceStatusRow())
    ).flatten


  private def lppManual(lpp: LPPDetails)(implicit messages: Messages): Seq[SummaryListRow] =
    Seq(
      lppSummaryRow.addedOnRow(lpp)
    ).flatten
}
