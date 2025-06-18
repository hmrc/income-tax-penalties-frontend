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

object LSPCardMessages {

  sealed trait Messages { _: i18n =>
    //Card Title Messages
    val cardTitleAdjustmentPoint: Int => String = point => s"Penalty point $point: Adjustment point"
    val cardTitleRemovedPoint = "Penalty point"
    def cardTitleFinancialPoint(point: Int, reason: String, amount: String): String =
      s"Penalty point $point$reason - £$amount penalty"
    def cardTitleAdditionalFinancialPoint(amount: String, reason: String): String =
      s"Additional £$amount penalty$reason"
    val cardTitlePoint: Int => String = point => "Penalty point " + point

    //Summary Row Messages
    val addedOnKey = "Added on"
    val updatePeriod = "Update period"
    def quarterValue(fromDate: String, toDate: String) = s"$fromDate to $toDate"
    val updateDueKey = "Update due"
    val updateSubmittedKey = "Update submitted"
    val returnNotReceived = "Return not received"
    val expiryDateKey = "Point due to expire"
    val expiryReasonKey = "Removed reason"

    //Card Footer Links
    val cardLinksAdjustedPointCannotAppeal = "You cannot appeal this point"
    val cardLinksFindOutHowToAppeal = "Find out how to appeal"
    val cardLinksAppealNoPointNumber = "Appeal penalty"
    val cardLinksAppeal: Int => String = point => s"Appeal penalty point $point"
    val cardLinksReviewAppeal: String = "Ask for review"
  }

  object English extends Messages with En

  object Welsh extends Messages with Cy {
    //Card Title Messages
    override val cardTitleAdjustmentPoint: Int => String = point => s"Pwynt cosb $point: pwynt addasu"
    override val cardTitleRemovedPoint = "Pwynt cosb"
    override def cardTitleFinancialPoint(point: Int, reason: String, amount: String): String =
      s"Penalty point $point$reason: £$amount penalty (Welsh)"
    override def cardTitleAdditionalFinancialPoint(amount: String, reason: String): String =
      s"Cosb ychwanegol o £$amount$reason"

    override val cardTitlePoint: Int => String = point => s"Pwynt cosb $point"

    //Summary Row Messages
    override val addedOnKey = "Ychwanegwyd ar"
    override val updatePeriod = "Update period (Welsh)"
    override def quarterValue(fromDate: String, toDate: String) = s"$fromDate to $toDate (Welsh)"
    override val updateDueKey = "Diweddariad i’w gyflwyno"
    override val updateSubmittedKey = "Diweddariad wedi’i gyflwyno"
    override val returnNotReceived = "Return not received (Welsh)"
    override val expiryDateKey = "Pwynt cosb yn dod i ben"
    override val expiryReasonKey = "Removed reason (Welsh)"

    //Card Footer Links
    override val cardLinksAdjustedPointCannotAppeal = "YNi allwch apelio yn erbyn y pwynt hwn"
    override val cardLinksFindOutHowToAppeal = "Find out how to appeal (Welsh)"
    override val cardLinksAppealNoPointNumber = "Apelio yn erbyn cosb"
    override val cardLinksAppeal: Int => String = point => s"Apelio yn erbyn pwynt cosb $point"
    override val cardLinksReviewAppeal: String = "Gofyn am adolygiad"
  }
}
