/*
 * Copyright 2026 HM Revenue & Customs
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

import uk.gov.hmrc.incometaxpenaltiesfrontend.models.penaltyDetails.appealInfo.AppealStatusEnum

sealed trait LPPTabMessage

object LPPTabMessage {
  case object None extends LPPTabMessage
  case object NoPenalties extends LPPTabMessage
  case object PayPenaltyNowSingle extends LPPTabMessage
  case object PayPenaltyNowMultiple extends LPPTabMessage
  case object PayEarlyInfo extends LPPTabMessage
}

case class LPPTabViewModel(message: LPPTabMessage) {
  def messageKeys: Seq[String] = message match {
    case LPPTabMessage.NoPenalties => Seq("individual.lpp.noPenalties")
    case LPPTabMessage.PayPenaltyNowSingle => Seq(
      "individual.lpp.pay.penalty.now",
      "individual.lpp.penalty.no.longer.estimate"
    )
    case LPPTabMessage.PayPenaltyNowMultiple => Seq(
      "individual.lpp.pay.penalties.now",
      "individual.lpp.penalties.no.longer.estimate"
    )
    case LPPTabMessage.PayEarlyInfo => Seq("individual.index.lpp.tab.payEarlyInfo")
    case LPPTabMessage.None => Seq.empty
  }
}

object LPPTabViewModel {

  def apply(lppCardData: Seq[LatePaymentPenaltySummaryCard], isInBreathingSpace: Boolean): LPPTabViewModel = {
    if (lppCardData.isEmpty) {
      LPPTabViewModel(LPPTabMessage.NoPenalties)
    } else {
      val hasNonUpheldAppeal = lppCardData.exists(lpp => !lpp.appealStatus.contains(AppealStatusEnum.Upheld))

      if (!isInBreathingSpace && hasNonUpheldAppeal) {
        if (lppCardData.forall(_.isPenaltyPaid)) {
          LPPTabViewModel(LPPTabMessage.NoPenalties)
        } else {
          val unpaidTaxPaidCount = lppCardData.count(lpp => lpp.incomeTaxIsPaid && !lpp.isPenaltyPaid)

          if (unpaidTaxPaidCount == 1) {
            LPPTabViewModel(LPPTabMessage.PayPenaltyNowSingle)
          } else if (unpaidTaxPaidCount > 1) {
            LPPTabViewModel(LPPTabMessage.PayPenaltyNowMultiple)
          } else {
            LPPTabViewModel(LPPTabMessage.PayEarlyInfo)
          }
        }
      } else {
        LPPTabViewModel(LPPTabMessage.None)
      }
    }
  }
}


