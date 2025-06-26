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
import uk.gov.hmrc.incometaxpenaltiesfrontend.utils.DateFormatter
import uk.gov.hmrc.incometaxpenaltiesfrontend.viewModels.FirstLatePaymentPenaltyCalculationData

class FirstLatePaymentCalculationHelper {


  def getPaymentDetails(calculationData: FirstLatePaymentPenaltyCalculationData,
                        isAgent: Boolean)(implicit messages: Messages): Option[String] = {

    if(calculationData.llpHRCharge.isEmpty && !calculationData.incomeTaxIsPaid) {
      None
    } else {
      Some{
        if(calculationData.isPenaltyPaid) {
          messages("calculation.individual.paid.penalty.on", "22 December 2024")
        } else {
          messages("calculation.individual.pay.penalty.by", DateFormatter.dateToString(calculationData.payPenaltyBy))
        }
      }
    }
  }

  def getMissedDeadlineMsg(calculationData: FirstLatePaymentPenaltyCalculationData,
                        isAgent: Boolean)(implicit messages: Messages): String = {
    calculationData.llpHRCharge match {
      case Some(_) =>  messages("calculation.individual.payment.missed.reason.additional")
      case None if !calculationData.incomeTaxIsPaid =>
        messages("calculation.individual.payment.15.30.missed.reason.taxUnpaid")
      case _ => messages("calculation.individual.payment.15.30.missed.reason")
    }
  }

  def getFirstBulletPointMsg(calculationData: FirstLatePaymentPenaltyCalculationData,
                        isAgent: Boolean)(implicit messages: Messages): String = {
    calculationData.llpHRCharge match {
      case Some(_) =>  messages("calculation.individual.payment.30.plus.unpaid.missed.reason.bullet.1", calculationData.llpLRCharge.chargeAmount)
      case None if !calculationData.incomeTaxIsPaid =>
        messages("calculation.individual.payment.15.30.unpaid.missed.reason.bullet.1", calculationData.llpLRCharge.chargeAmount)
      case _ => messages("calculation.individual.payment.15.30.missed.reason.bullet.1", calculationData.llpLRCharge.chargeAmount)
    }
  }

  def getSecondBulletPointMsg(calculationData: FirstLatePaymentPenaltyCalculationData,
                        isAgent: Boolean)(implicit messages: Messages): String = {
    calculationData.llpHRCharge match {
      case Some(_) =>  messages("calculation.individual.payment.30.plus.missed.reason.bullet.2", calculationData.llpHRCharge)
      case None if !calculationData.incomeTaxIsPaid =>
        messages("calculation.individual.payment.15.30.unpaid.missed.reason.bullet.2")
    }
  }

}
