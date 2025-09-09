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
    val isAgentTag = if(isAgent) "agent" else "individual"
    if(!calculationData.incomeTaxIsPaid && calculationData.isEstimate) {
      messages(s"calculation.$isAgentTag.calc2.penalty.isEstimate",
        dateToYearString(calculationData.taxPeriodStartDate),
        dateToYearString(calculationData.taxPeriodEndDate))
    } else if(calculationData.isPenaltyOverdue) {
      messages("calculation.individual.calc2.penalty.overdue")
    } else {
      messages(s"calculation.$isAgentTag.calc2.penalty.due", dateToString(calculationData.payPenaltyBy))
    }
  }

}
