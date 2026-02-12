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
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.penaltyDetails.breathingSpace.BreathingSpace
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.penaltyDetails.lpp.LPPPenaltyStatusEnum
import uk.gov.hmrc.incometaxpenaltiesfrontend.utils.{DateFormatter, TimeMachine}
import uk.gov.hmrc.incometaxpenaltiesfrontend.utils.DateFormatter.{dateToString, dateToYearString}
import uk.gov.hmrc.incometaxpenaltiesfrontend.viewModels.SecondLatePaymentPenaltyCalculationData

class SecondLatePaymentCalculationHelper {


  def getPaymentDetails(calculationData: SecondLatePaymentPenaltyCalculationData)(implicit messages: Messages): String = {

    if (calculationData.isPenaltyPaid) {
      messages("calculation.individual.paid.penalty.on", DateFormatter.dateToString(calculationData.payPenaltyBy))
    } else {
      messages("calculation.individual.pay.penalty.by", DateFormatter.dateToString(calculationData.payPenaltyBy))
    }
  }


  def getFinalUnpaidMsg(calculationData: SecondLatePaymentPenaltyCalculationData,
                        isAgent: Boolean)(implicit messages: Messages): String = {
    val isAgentTag = if (isAgent) "agent" else "individual"
    if (!calculationData.incomeTaxIsPaid && calculationData.isEstimate) {
      val isEstimateMsg = messages(s"calculation.$isAgentTag.calc2.penalty.isEstimate",
        dateToYearString(calculationData.taxPeriodStartDate),
        dateToYearString(calculationData.taxPeriodEndDate))
      val toStopEstimateIncMsg = messages(s"calculation.$isAgentTag.calc2.penalty.stopEstimateIncreasing")
      if (calculationData.paymentPlanAgreed.isDefined || calculationData.paymentPlanProposed.isDefined) {
        isEstimateMsg
      } else {
        isEstimateMsg + " " + toStopEstimateIncMsg
      }
    } else if (calculationData.isPenaltyOverdue) {
      messages("calculation.individual.calc2.penalty.overdue")
    } else {
      messages(s"calculation.$isAgentTag.calc2.penalty.due", dateToString(calculationData.principalChargeDueDate))
    }
  }

  def getPaymentPlanInset(calculationData: SecondLatePaymentPenaltyCalculationData, individualOrAgent: String)(implicit messages: Messages): Option[String] = {
    (calculationData.paymentPlanAgreed, calculationData.paymentPlanProposed) match {
      case (_, Some(proposedDate)) =>
        Some(messages(s"calculation.$individualOrAgent.calc2.penalty.payment.plan.proposed.inset", dateToString(proposedDate)))
      case _ => None
    }
  }

  def getPaymentPlanHeading(calculationData: SecondLatePaymentPenaltyCalculationData, individualOrAgent: String)(implicit messages: Messages): Option[String] = {
    (calculationData.paymentPlanAgreed, calculationData.paymentPlanProposed) match {
      case (Some(agreedDate), _) =>
        Some(messages(s"calculation.$individualOrAgent.calc2.penalty.payment.plan.agreed.h1"))
      case _ => None
    }
  }

  def getPaymentPlanContent(calculationData: SecondLatePaymentPenaltyCalculationData, individualOrAgent: String)(implicit messages: Messages): List[String] = {
    (calculationData.paymentPlanAgreed, calculationData.paymentPlanProposed) match {
      case (Some(agreedDate), _) =>
        calculationData.paymentPlanAgreed.map { agreedDate =>
          List(
            messages(s"calculation.$individualOrAgent.calc2.penalty.payment.plan.agreed.p1", dateToString(agreedDate)),
            messages(s"calculation.$individualOrAgent.calc2.penalty.payment.plan.agreed.p2"),
            messages(s"calculation.$individualOrAgent.calc2.penalty.payment.plan.agreed.p3")
          )
        }.getOrElse(List.empty)
      case _ => List.empty
    }
  }

  def isExpiredBreathingSpace(calculationData: SecondLatePaymentPenaltyCalculationData,
                              breathingSpaceData: Option[Seq[BreathingSpace]],
                              timeMachine: TimeMachine): Boolean = {
    breathingSpaceData match {
      case Some(breathingSpace) => breathingSpace.count(bs =>
        (bs.bsEndDate.isBefore(timeMachine.getCurrentDate()) && !bs.bsEndDate.isBefore(calculationData.principalChargeDueDate.plusDays(31))) &&
          (
            (calculationData.penaltyStatus == LPPPenaltyStatusEnum.Accruing && bs.bsEndDate.isAfter(calculationData.principalChargeDueDate.plusDays(30))) ||
              (calculationData.penaltyStatus == LPPPenaltyStatusEnum.Posted &&
                (
                  (bs.bsStartDate.isAfter(calculationData.principalChargeDueDate.plusDays(30)) && bs.bsStartDate.isBefore(calculationData.penaltyChargeCreationDate.get.plusDays(1))) ||
                    (bs.bsEndDate.isAfter(calculationData.principalChargeDueDate.plusDays(30)) && bs.bsEndDate.isBefore(calculationData.penaltyChargeCreationDate.get.plusDays(1))) ||
                    (bs.bsStartDate.isBefore(calculationData.principalChargeDueDate.plusDays(31)) && bs.bsEndDate.isAfter(calculationData.penaltyChargeCreationDate.get))
                  )
                )
            )
      ) > 0
      case None => false
    }
  }
}
