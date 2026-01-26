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

package uk.gov.hmrc.incometaxpenaltiesfrontend.penaltyDetails.users.lsp

import org.jsoup.nodes.Element
import uk.gov.hmrc.incometaxpenaltiesfrontend.penaltyDetails.users.UserDetailsData

object AA511110AOverdue extends UserDetailsData {

  override val nino: String = AA511110A.nino
  override val hasFinancialLSP: Boolean = AA511110A.hasFinancialLSP
  override val numberOfLSPPenalties: Int = AA511110A.numberOfLSPPenalties
  override val numberOfPaidFinancialPenalties: Int = AA511110A.numberOfPaidFinancialPenalties
  override val numberOfUnpaidFinancialPenalties: Int = AA511110A.numberOfUnpaidFinancialPenalties

  override val expectedNumberOfLSPPenaltyCards: Int = AA511110A.expectedNumberOfLSPPenaltyCards
  override val expectedNumberOfLPPPenaltyCards: Int = AA511110A.expectedNumberOfLPPPenaltyCards

  def penaltyCard0ExpectedContent(card: Element): Unit = {
    validatePenaltyCardTitle(card, expectedTitle = "Additional £200 penalty: Late tax return")
    validateCardTag(card, expectedTag = "Overdue")
    val cardRows = getCardsRows(card)
    cardRows.size() shouldBe 5
    validateSummary(cardRows.get(0), "Pay penalty by", "17 March 2029")
    validateSummary(cardRows.get(1), "Tax year", "2027 to 2028")
    validateSummary(cardRows.get(2), "Return due", "31 January 2029")
    validateSummary(cardRows.get(3), "Return submitted", "Not yet received")
    validateSummary(cardRows.get(4), "Appeal status", "Under review")
  }

  override val expectedPenaltyCardsContent: Map[Int, Element => Unit] = Map(
    0 -> penaltyCard0ExpectedContent,
    1 -> AA511110A.penaltyCard1ExpectedContent,
    2 -> AA511110A.penaltyCard2ExpectedContent,
    3 -> AA511110A.penaltyCard3ExpectedContent,
    4 -> AA511110A.penaltyCard4ExpectedContent
  )

  override val expectedOverviewText: Boolean => String = AA511110A.expectedOverviewText

  override val timeMachineDate: Option[String] = Some("30/04/2029")
}
