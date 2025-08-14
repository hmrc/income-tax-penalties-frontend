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

import uk.gov.hmrc.incometaxpenaltiesfrontend.models.penaltyDetails.lpp.LPPPenaltyCategoryEnum.{LPP1, LPP2}
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.penaltyDetails.lpp.LPPPenaltyCategoryEnum

object LPPCardMessages {

  sealed trait Messages { _: i18n =>

    //Card Title Messages
    val cardTitlePenalty: String => String = amount => s"£$amount penalty"
    val cardTitleFirstPenalty: String => String = amount => s"First late payment penalty: £$amount"
    val cardTitleSecondPenalty: String => String = amount => s"Second late payment penalty: £$amount"
    val cardTitlePenaltyDetailsLetter: String => String = amount => "Penalty for late payment - details are in the letter we sent you"

    //Summary Row Messages
    val penaltyTypeKey = "Penalty type"
    val penaltyTypeValue: LPPPenaltyCategoryEnum.Value => String = {
      case LPP1 => "First late payment penalty"
      case LPP2 => "Second late payment penalty"
      case _    => "Penalty for late payment - details are in the letter we sent you"
    }
    val incomeTaxPeriodKey = "Overdue charge"
    def overdueChargeValue(fromYear: String, toYear: String) = s"Income Tax for $fromYear to $toYear tax year"
    val addedOnKey = "Added on"
    val incomeTaxDueKey = "Income Tax due"
    val incomeTaxPaidKey = "Income Tax paid"
    val paymentNotReceived = "Payment not yet received"

    //Card Footer Links
    val cardLinksViewCalculation = "View calculation"
    val cardLinksReviewAppeal = "Ask for review"
    val cardLinksCheckIfCanAppeal = "Check if you can appeal this penalty"
    val cannotAppeal = "You cannot appeal this point"
  }

  object English extends Messages with En

  object Welsh extends Messages with Cy {

    //Card Title Messages
    override val cardTitlePenalty: String => String = amount => s"Cosb o £$amount"
    override val cardTitleFirstPenalty: String => String = amount => s"First late payment penalty: £$amount (Welsh)"
    override val cardTitleSecondPenalty: String => String = amount => s"Second late payment penalty: £$amount (Welsh)"
    override val cardTitlePenaltyDetailsLetter: String => String = amount => "Penalty for late payment - details are in the letter we sent you (Welsh)"

    //Summary Row Messages
    override val penaltyTypeKey = "Math o gosb"
    override val penaltyTypeValue: LPPPenaltyCategoryEnum.Value => String = {
      case LPP1 => "First late payment penalty (Welsh)"
      case LPP2 => "Second late payment penalty (Welsh)"
      case _    => "Penalty for late payment - details are in the letter we sent you (Welsh)"
    }
    override val incomeTaxPeriodKey = "Taliad sy’n hwyr"
    override def overdueChargeValue(fromYear: String, toYear: String) = s"Treth Incwm ar gyfer blwyddyn dreth $fromYear i $toYear"
    override val addedOnKey = "Ychwanegwyd ar"
    override val incomeTaxDueKey = "Treth Incwm sy’n ddyledus"
    override val incomeTaxPaidKey = "Treth Incwm wedi’i thalu"
    override val paymentNotReceived = "Nid yw’r taliad wedi dod i law hyd yn hyn"

    //Card Footer Links
    override val cardLinksViewCalculation = "Gweld y cyfrifiad"
    override val cardLinksCheckIfCanAppeal = "Check if you can appeal this penalty (Welsh)"
    override val cardLinksReviewAppeal = "Gofyn am adolygiad"
    override val cannotAppeal = "YNi allwch apelio yn erbyn y pwynt hwn"
  }
}
