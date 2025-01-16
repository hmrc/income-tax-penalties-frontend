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

package fixtures.messages

import uk.gov.hmrc.incometaxpenaltiesfrontend.models.lpp.LPPPenaltyCategoryEnum
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.lpp.LPPPenaltyCategoryEnum.{LPP1, LPP2, MANUAL}

object LPPCardMessages {

  sealed trait Messages { _: i18n =>

    //Card Title Messages
    val cardTitlePenalty: String => String = amount => s"£$amount penalty"

    //Summary Row Messages
    val penaltyTypeKey = "Penalty type"
    val penaltyTypeValue: LPPPenaltyCategoryEnum.Value => String = {
      case LPP1   => "First penalty for late payment"
      case LPP2   => "Second penalty for late payment"
      case MANUAL => "Penalty for late payment - details are in the letter we sent you"
    }
    val incomeTaxPeriodKey = "Overdue charge"
    def overdueChargeValue(fromYear: String, toYear: String) = s"Income Tax for $fromYear to $toYear tax year"
    val addedOnKey = "Added on"
    val incomeTaxDueKey = "Income Tax due"
    val incomeTaxPaidKey = "Income Tax paid"
    val paymentNotReceived = "Payment not yet received"

    //Card Footer Links
    val cardLinksViewCalculation = "View calculation"
    val cardLinksAppealThisPenalty = "Appeal this penalty"
    val cardLinksFindOutHowToAppeal = "Find out how to appeal"
    val cannotAppeal = "You cannot appeal this point"
  }

  object English extends Messages with En

  object Welsh extends Messages with Cy {

    //Card Title Messages
    override val cardTitlePenalty: String => String = amount => s"£$amount penalty (Welsh)"

    //Summary Row Messages
    override val penaltyTypeKey = "Penalty type (Welsh)"
    override val penaltyTypeValue: LPPPenaltyCategoryEnum.Value => String = {
      case LPP1   => "First penalty for late payment (Welsh)"
      case LPP2   => "Second penalty for late payment (Welsh)"
      case MANUAL => "Penalty for late payment - details are in the letter we sent you (Welsh)"
    }
    override val incomeTaxPeriodKey = "Overdue charge (Welsh)"
    override def overdueChargeValue(fromYear: String, toYear: String) = s"Income Tax for $fromYear to $toYear tax year (Welsh)"
    override val addedOnKey = "Added on (Welsh)"
    override val incomeTaxDueKey = "Income Tax due (Welsh)"
    override val incomeTaxPaidKey = "Income Tax paid (Welsh)"
    override val paymentNotReceived = "Payment not yet received (Welsh)"

    //Card Footer Links
    override val cardLinksViewCalculation = "View calculation (Welsh)"
    override val cardLinksAppealThisPenalty = "Appeal this penalty (Welsh)"
    override val cardLinksFindOutHowToAppeal = "Find out how to appeal (Welsh)"
    override val cannotAppeal = "You cannot appeal this point (Welsh)"
  }
}
