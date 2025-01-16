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

object LPPCardMessages {

  sealed trait Messages { _: i18n =>

    //Card Title Messages
    val cardTitlePenalty: String => String = amount => s"£$amount penalty"

    //Summary Row Messages
    val penaltyTypeKey = "Penalty type"
    val overdueChargeKey = "Overdue charge"
    def overdueChargeValue(fromYear: String, toYear: String) = s"Income Tax for $fromYear to $toYear tax year"
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
    override val overdueChargeKey = "Overdue charge (Welsh)"
    override def overdueChargeValue(fromYear: String, toYear: String) = s"Income Tax for $fromYear to $toYear tax year (Welsh)"
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
