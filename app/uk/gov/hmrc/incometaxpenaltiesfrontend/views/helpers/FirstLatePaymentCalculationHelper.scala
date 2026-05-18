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
import uk.gov.hmrc.incometaxpenaltiesfrontend.utils.{DateFormatter, TimeMachine}
import uk.gov.hmrc.incometaxpenaltiesfrontend.utils.DateFormatter.{dateToString, dateToYearString}
import uk.gov.hmrc.incometaxpenaltiesfrontend.viewModels.FirstLatePaymentPenaltyCalculationData

class FirstLatePaymentCalculationHelper {


  def getPaymentDetails(calculationData: FirstLatePaymentPenaltyCalculationData)(implicit messages: Messages): Option[String] = {

    if (calculationData.llpHRCharge.isEmpty && !calculationData.incomeTaxIsPaid) {
      None
    } else {
      Some {
        if (calculationData.isPenaltyPaid) {
          messages("calculation.paid.penalty.on", DateFormatter.dateToString(calculationData.payPenaltyBy))
        } else {
          messages("calculation.pay.penalty.by", DateFormatter.dateToString(calculationData.payPenaltyBy))
        }
      }
    }
  }

  def getMissedDeadlineMsg(calculationData: FirstLatePaymentPenaltyCalculationData)(implicit messages: Messages): String = {
    calculationData.llpHRCharge match {
      case Some(_) => messages("calculation.payment.missed.reason.additional")
      case None if !calculationData.incomeTaxIsPaid =>
        messages("calculation.payment.15.30.missed.reason.taxUnpaid")
      case None if calculationData.isPenaltyPaid =>
        messages("calculation.payment.15.30.missed.reason.taxPaid")

      case _ => messages("calculation.payment.15.30.missed.reason.taxDueOrOverdue") 
    }
  }



  def getFinalUnpaidMsg(calculationData: FirstLatePaymentPenaltyCalculationData,
                        isPfa: String)(implicit messages: Messages): Option[String] = {
    if (calculationData.llpHRCharge.isEmpty && !calculationData.incomeTaxIsPaid) {
      val isEstimateMsg = messages(s"calculation.penalty.isEstimate$isPfa", dateToYearString(calculationData.taxPeriodStartDate), dateToYearString(calculationData.taxPeriodEndDate))
      val toStopEstimateIncMsg = messages(s"calculation.penalty.stopEstimateIncreasing$isPfa")
      if (calculationData.paymentPlanAgreed.isDefined || calculationData.paymentPlanProposed.isDefined) {
        Some(isEstimateMsg)
      } else {
        Some(isEstimateMsg + " " + toStopEstimateIncMsg)
      }
    } else {
      None
    }
  }

  def getPaymentPlanInset(calculationData: FirstLatePaymentPenaltyCalculationData)(implicit messages: Messages): Option[String] = {
    (calculationData.paymentPlanAgreed, calculationData.paymentPlanProposed) match {
      case (_, Some(proposedDate)) =>
        Some(messages("calculation.penalty.payment.plan.proposed.inset", dateToString(proposedDate)))
      case _ => None
    }
  }

  def getPaymentPlanHeading(calculationData: FirstLatePaymentPenaltyCalculationData)(implicit messages: Messages): Option[String] = {
    (calculationData.paymentPlanAgreed, calculationData.paymentPlanProposed) match {
      case (Some(agreedDate), _) =>
          Some(messages("calculation.penalty.payment.plan.agreed.h1", agreedDate))
      case _ => None
    }
  }

  def getPaymentPlanContent(calculationData: FirstLatePaymentPenaltyCalculationData)(implicit messages: Messages): List[String] = {
    (calculationData.paymentPlanAgreed, calculationData.paymentPlanProposed) match {
      case (Some(agreedDate), _) =>
        calculationData.paymentPlanAgreed.map { agreedDate =>
          List(
            messages("calculation.penalty.payment.plan.agreed.p1", dateToString(agreedDate)),
            messages("calculation.penalty.payment.plan.agreed.p2"),
            messages(s"calculation.penalty.payment.plan.agreed.p3")
          )
        }.getOrElse(List.empty)
      case _ => List.empty
    }
  }
  
  def isExpiredBreathingSpace(calculationData: FirstLatePaymentPenaltyCalculationData,
                              breathingSpaceData: Option[Seq[BreathingSpace]],
                              timeMachine: TimeMachine): Boolean = {
    breathingSpaceData match {
      case Some(breathingSpace) => breathingSpace.count(bs =>
        (bs.bsEndDate.isBefore(timeMachine.getCurrentDate()) && !bs.bsEndDate.isBefore(calculationData.principalChargeDueDate)) && (
          (bs.bsStartDate.isAfter(calculationData.principalChargeDueDate) && bs.bsStartDate.isBefore(calculationData.principalChargeDueDate.plusDays(31))) ||
            (bs.bsEndDate.isAfter(calculationData.principalChargeDueDate) && bs.bsEndDate.isBefore(calculationData.principalChargeDueDate.plusDays(31))) ||
            (bs.bsStartDate.isBefore(calculationData.principalChargeDueDate.plusDays(1)) && bs.bsEndDate.isAfter(calculationData.principalChargeDueDate.plusDays(30)))
        )) > 0
      case None => false
    }
  }
}
