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
import uk.gov.hmrc.incometaxpenaltiesfrontend.utils.DateFormatter
import uk.gov.hmrc.incometaxpenaltiesfrontend.utils.DateFormatter.{dateToString, dateToYearString}
import uk.gov.hmrc.incometaxpenaltiesfrontend.viewModels.FirstLatePaymentPenaltyCalculationData

class FirstLatePaymentCalculationHelper {


  def getPaymentDetails(calculationData: FirstLatePaymentPenaltyCalculationData,
                        isAgent: Boolean)(implicit messages: Messages): Option[String] = {

    if (calculationData.llpHRCharge.isEmpty && !calculationData.incomeTaxIsPaid) {
      None
    } else {
      Some {
        if (calculationData.isPenaltyPaid) {
          messages("calculation.individual.paid.penalty.on", DateFormatter.dateToString(calculationData.payPenaltyBy))
        } else {
          messages("calculation.individual.pay.penalty.by", DateFormatter.dateToString(calculationData.payPenaltyBy))
        }
      }
    }
  }

  def getMissedDeadlineMsg(calculationData: FirstLatePaymentPenaltyCalculationData,
                           individualOrAgent: String)(implicit messages: Messages): String = {
    calculationData.llpHRCharge match {
      case Some(_) => messages(s"calculation.$individualOrAgent.payment.missed.reason.additional")
      case None if !calculationData.incomeTaxIsPaid =>
        messages(s"calculation.$individualOrAgent.payment.15.30.missed.reason.taxUnpaid")
      case _ => messages(s"calculation.$individualOrAgent.payment.15.30.missed.reason")
    }
  }

  def getBulletListContent(calculationData: FirstLatePaymentPenaltyCalculationData,
                           individualOrAgent: String, isPfa: String)(implicit messages: Messages): List[String] = {
    calculationData.llpHRCharge match {
      case Some(llpHRCharge) => List(
        messages(s"calculation.individual.payment.30.plus.unpaid.missed.reason.bullet.1$isPfa", calculationData.llpLRCharge.formattedChargeAmount),
        messages(s"calculation.individual.payment.30.plus.missed.reason.bullet.2$isPfa", llpHRCharge.formattedChargeAmount)
      )
      case None if !calculationData.incomeTaxIsPaid => List(
        messages(s"calculation.$individualOrAgent.payment.15.30.unpaid.missed.reason.bullet.1$isPfa", calculationData.llpLRCharge.formattedChargeAmount),
        messages(s"calculation.$individualOrAgent.payment.15.30.unpaid.missed.reason.bullet.2$isPfa")
      )
      case None => List(
        messages(s"calculation.$individualOrAgent.payment.15.30.missed.reason.bullet.1$isPfa", calculationData.llpLRCharge.formattedChargeAmount)
      )
    }
  }

  def getFinalUnpaidMsg(calculationData: FirstLatePaymentPenaltyCalculationData,
                        individualOrAgent: String, isPfa: String)(implicit messages: Messages): String = {
    if (calculationData.llpHRCharge.isEmpty && !calculationData.incomeTaxIsPaid) {
      val isEstimateMsg = messages(s"calculation.$individualOrAgent.penalty.isEstimate$isPfa", dateToYearString(calculationData.taxPeriodStartDate), dateToYearString(calculationData.taxPeriodEndDate))
      val toStopEstimateIncMsg = messages(s"calculation.$individualOrAgent.penalty.stopEstimateIncreasing$isPfa")
      if (calculationData.paymentPlanAgreed.isDefined || calculationData.paymentPlanProposed.isDefined) {
        isEstimateMsg
      } else {
        isEstimateMsg + " " + toStopEstimateIncMsg
      }
    } else if (calculationData.isPenaltyOverdue) {

      messages("calculation.individual.penalty.isOverdue")
    } else {
      messages(s"calculation.$individualOrAgent.penalty.isDue", dateToString(calculationData.payPenaltyBy))
    }
  }

  def getPaymentPlanInset(calculationData: FirstLatePaymentPenaltyCalculationData, individualOrAgent: String)(implicit messages: Messages): Option[String] = {
    (calculationData.paymentPlanAgreed, calculationData.paymentPlanProposed) match {
      case (_, Some(proposedDate)) =>
        Some(messages(s"calculation.$individualOrAgent.penalty.payment.plan.proposed.inset", dateToString(proposedDate)))
      case _ => None
    }
  }

  def getPaymentPlanHeading(calculationData: FirstLatePaymentPenaltyCalculationData, individualOrAgent: String)(implicit messages: Messages): Option[String] = {
    (calculationData.paymentPlanAgreed, calculationData.paymentPlanProposed) match {
      case (Some(agreedDate), _) =>
          Some(messages(s"calculation.$individualOrAgent.penalty.payment.plan.agreed.h1", agreedDate))
      case _ => None
    }
  }

  def getPaymentPlanContent(calculationData: FirstLatePaymentPenaltyCalculationData, individualOrAgent: String)(implicit messages: Messages): List[String] = {
    (calculationData.paymentPlanAgreed, calculationData.paymentPlanProposed) match {
      case (Some(agreedDate), _) =>
        calculationData.paymentPlanAgreed.map { agreedDate =>
          List(
            messages(s"calculation.$individualOrAgent.penalty.payment.plan.agreed.p1", agreedDate),
            messages(s"calculation.$individualOrAgent.penalty.payment.plan.agreed.p2"),
            messages(s"calculation.$individualOrAgent.penalty.payment.plan.agreed.p3")
          )
        }.getOrElse(List.empty)
      case _ => List.empty
    }
  }
  
  def isExpiredBreathingSpace(breathingSpaceData: Option[Seq[BreathingSpace]]): Boolean = {
    breathingSpaceData.isDefined
    //TODO: make this only return true if penalty was accruing during a breathing space period that is no longer active
  }
}
