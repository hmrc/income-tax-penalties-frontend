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
    def cardTitleFinancialPointNoThreshold(point: Int, reason: String): String =
      s"Penalty point $point$reason"
    def cardTitleAdditionalFinancialPoint(amount: String, reason: String): String =
      s"Additional £$amount penalty$reason"
    val cardTitlePoint: Int => String = point => "Penalty point " + point

    //Summary Row Messages
    val addedOnKey = "Added on"
    val updatePeriod = "Update period"
    def quarterValue(fromDate: String, toDate: String) = s"$fromDate to $toDate"
    val updateDueKey = "Update due"
    val updateSubmittedKey = "Update submitted"
    val returnNotReceived = "Not yet received"
    val expiryDateKey = "Point due to expire"
    val expiryReasonKey = "Removed reason"
    val payPenaltyBy = "Pay Penalty By"
    val missingOrLateIncomeSources = "Missing or late income sources"
    val lateUpdate = "Late update"

    //Card Footer Links
    val cardLinksAdjustedPointCannotAppeal = "You cannot appeal this point"
    val cardLinksFindOutHowToAppeal = "Check if you can appeal this penalty"
    val cardLinksAppealNoPointNumber = "Appeal penalty"
    val cardLinksAppeal: String = s"Check if you can appeal this penalty"
    val cardLinksReviewAppeal: String = "Ask for review"
  }

  object English extends Messages with En

  object Welsh extends Messages with Cy {
    //Card Title Messages
    override val cardTitleAdjustmentPoint: Int => String = point => s"Pwynt cosb $point: pwynt addasu"
    override val cardTitleRemovedPoint = "Pwynt cosb"
    override def cardTitleFinancialPoint(point: Int, reason: String, amount: String): String =
      s"Pwynt cosb $point$reason - Cosb o £$amount"
    override def cardTitleFinancialPointNoThreshold(point: Int, reason: String): String =
      s"Pwynt cosb $point$reason"
    override def cardTitleAdditionalFinancialPoint(amount: String, reason: String): String =
      s"Cosb ychwanegol o £$amount$reason"

    override val cardTitlePoint: Int => String = point => s"Pwynt cosb $point"

    //Summary Row Messages
    override val addedOnKey = "Ychwanegwyd ar"
    override val updatePeriod = "Cyfnod diweddaru"
    override def quarterValue(fromDate: String, toDate: String) = s"$fromDate i $toDate"
    override val updateDueKey = "Diweddariad i’w gyflwyno"
    override val updateSubmittedKey = "Diweddariad wedi’i gyflwyno"
    override val returnNotReceived = "Heb ddod i law eto"
    override val expiryDateKey = "Pwynt cosb yn dod i ben"
    override val expiryReasonKey = "Removed reason (Welsh)"
    override val payPenaltyBy = "Mae’n rhaid talu’r gosb erbyn"
    override val missingOrLateIncomeSources = "Ffynonellau incwm sy’n hwyr neu ar goll"
    override val lateUpdate = "Diweddariad hwyr"

    //Card Footer Links
    override val cardLinksAdjustedPointCannotAppeal = "Ni allwch apelio yn erbyn y pwynt hwn"
    override val cardLinksFindOutHowToAppeal = "Gwirio a allwch apelio yn erbyn y gosb hon"
    override val cardLinksAppealNoPointNumber = "Apelio yn erbyn cosb"
    override val cardLinksAppeal: String =  "Gwirio a allwch apelio yn erbyn y gosb hon"
    override val cardLinksReviewAppeal: String = "Gofyn am adolygiad"
  }
}
