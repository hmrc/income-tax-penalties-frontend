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
import uk.gov.hmrc.incometaxpenaltiesfrontend.penaltyDetails.users.lpp.AL300003A.{getCardsRows, validateCardTag, validatePenaltyCardTitle, validateSummary}

object AB311120A extends UserDetailsData {

  override val nino: String = "AB311120A"
  override val hasFinanicalLSP: Boolean = true
  override val numberOfLSPPenalties: Int = 2
  override val numberOfFinancialPenalties: Int = 2

  override val expectedNumberOfLSPPenaltyCards: Int = 3
  override val expectedNumberOfLPPPenaltyCards: Int = 0

  def penaltyCard0ExpectedContent(card: Element): Unit = {
    validatePenaltyCardTitle(card, expectedTitle = "Additional £200 penalty: Late tax return")
    validateCardTag(card, expectedTag = "Due")
    val cardRows = getCardsRows(card)
    cardRows.size() shouldBe 5
    validateSummary(cardRows.get(0), "Pay penalty by", "16 March 2028")
    validateSummary(cardRows.get(1), "Tax year", "2026 to 2027")
    validateSummary(cardRows.get(2), "Return due", "31 January 2028")
    validateSummary(cardRows.get(3), "Return submitted", "Not yet received")
    validateSummary(cardRows.get(4), "Appeal status", "Appeal in progress")
  }

  def penaltyCard1ExpectedContent(card: Element): Unit = {
    validatePenaltyCardTitle(card, expectedTitle = "Penalty point 2: Late tax return - £200 penalty")
    validateCardTag(card, expectedTag = "Overdue")
    val cardRows = getCardsRows(card)
    cardRows.size() shouldBe 5
    validateSummary(cardRows.get(0), "Pay penalty by", "17 March 2027")
    validateSummary(cardRows.get(1), "Tax year", "2025 to 2026")
    validateSummary(cardRows.get(2), "Return due", "31 January 2027")
    validateSummary(cardRows.get(3), "Return submitted", "22 February 2027")
    validateSummary(cardRows.get(4), "Appeal status", "Decision upheld")
  }

  def penaltyCard2ExpectedContent(card: Element): Unit = {
    validatePenaltyCardTitle(card, expectedTitle = "Penalty point 1: Late tax return")
    validateCardTag(card, expectedTag = "Active")
    val cardRows = getCardsRows(card)
    cardRows.size() shouldBe 4
    validateSummary(cardRows.get(0), "Tax year", "2024 to 2025")
    validateSummary(cardRows.get(1), "Return due", "31 January 2026")
    validateSummary(cardRows.get(2), "Return submitted", "22 February 2026")
    validateSummary(cardRows.get(3), "Appeal status", "Under review")
  }

  override val expectedPenaltyCardsContent: Map[Int, Element => Unit] = Map(
    0 -> penaltyCard0ExpectedContent,
    1 -> penaltyCard1ExpectedContent,
    2 -> penaltyCard2ExpectedContent
  )

  override val expectedOverviewText: Boolean => String = isAgent =>
    s"Overview Your${if (isAgent) " client’s" else ""} account has: late submission penalties the maximum number of late submission penalty points Check amounts${if (isAgent) "" else " and pay"}"
    
  override val timeMachineDate: Option[String] = Some("28/02/2028")
}
