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
import uk.gov.hmrc.incometaxpenaltiesfrontend.penaltyDetails.users.lpp.AL300003A.{getCardsRows, validateCardTag, validatePenaltyCardTitle, validateSummary, validateViewCalculationLink}

object AA000050A extends UserDetailsData {

  override val nino: String = "AA000050A"
  override val hasFinanicalLSP: Boolean = true
  override val numberOfLSPPenalties: Int = 0

  override val expectedNumberOfLSPPenaltyCards: Int = 5
  override val expectedNumberOfLPPPenaltyCards: Int = 0

  def penaltyCard0ExpectedContent(card: Element): Unit = {
    validatePenaltyCardTitle(card, expectedTitle = "Penalty point")
    validateCardTag(card, expectedTag = "Expired")
    val cardRows = getCardsRows(card)
    cardRows.size() shouldBe 5
    validateSummary(cardRows.get(0), "Missing or late income sources", "JB Painting and Decorating UK property rental income")
    validateSummary(cardRows.get(1), "Update period", "6 July 2026 to 5 October 2026")
    validateSummary(cardRows.get(2), "Update due", "7 November 2026")
    validateSummary(cardRows.get(3), "Update submitted", "1 December 2026")
    validateSummary(cardRows.get(4), "Point expired on", "5 October 2028")
  }

  def penaltyCard1ExpectedContent(card: Element): Unit = {
    validatePenaltyCardTitle(card, expectedTitle = "Penalty point")
    validateCardTag(card, expectedTag = "Expired")
    val cardRows = getCardsRows(card)
    cardRows.size() shouldBe 5
    validateSummary(cardRows.get(1), "Update period", "6 April 2026 to 5 July 2026")
    validateSummary(cardRows.get(2), "Update due", "7 August 2026")
    validateSummary(cardRows.get(3), "Update submitted", "1 September 2026")
    validateSummary(cardRows.get(4), "Point expired on", "5 July 2028")
  }

  def penaltyCard2ExpectedContent(card: Element): Unit = {
    validatePenaltyCardTitle(card, expectedTitle = "Penalty point")
    validateCardTag(card, expectedTag = "Expired")
    val cardRows = getCardsRows(card)
    cardRows.size() shouldBe 5
    validateSummary(cardRows.get(0), "Missing or late income sources", "JB Painting and Decorating UK property rental income")
    validateSummary(cardRows.get(1), "Update period", "6 January 2026 to 5 April 2026")
    validateSummary(cardRows.get(2), "Update due", "7 May 2026")
    validateSummary(cardRows.get(3), "Update submitted", "1 June 2026")
    validateSummary(cardRows.get(4), "Point expired on", "5 April 2028")
  }

  def penaltyCard3ExpectedContent(card: Element): Unit = {
    validatePenaltyCardTitle(card, expectedTitle = "Penalty point")
    validateCardTag(card, expectedTag = "Expired")
    val cardRows = getCardsRows(card)
    cardRows.size() shouldBe 5
    validateSummary(cardRows.get(0), "Missing or late income sources", "JB Painting and Decorating UK property rental income")
    validateSummary(cardRows.get(1), "Update period", "6 October 2025 to 5 January 2026")
    validateSummary(cardRows.get(2), "Update due", "7 February 2026")
    validateSummary(cardRows.get(3), "Update submitted", "1 March 2026")
    validateSummary(cardRows.get(4), "Point expired on", "5 January 2028")
  }


  def penaltyCard4ExpectedContent(card: Element): Unit = {
    validatePenaltyCardTitle(card, expectedTitle = "Penalty point")
    validateCardTag(card, expectedTag = "Expired")
    val cardRows = getCardsRows(card)
    cardRows.size() shouldBe 4
    validateSummary(cardRows.get(0), "Tax year", "2024 to 2025")
    validateSummary(cardRows.get(1), "Return due", "31 January 2026")
    validateSummary(cardRows.get(2), "Return submitted", "22 February 2026")
    validateSummary(cardRows.get(3), "Point expired on", "5 April 2027")
  }

  override val expectedPenaltyCardsContent: Map[Int, Element => Unit] = Map(
    0 -> penaltyCard0ExpectedContent,
    1 -> penaltyCard1ExpectedContent,
    2 -> penaltyCard2ExpectedContent,
    3 -> penaltyCard3ExpectedContent,
    4 -> penaltyCard4ExpectedContent
  )

  override val expectedOverviewText: Boolean => String = isAgent =>
    s"Overview Your account has: late submission penalties the maximum number of late submission penalty points Check amounts${if(isAgent) "" else " and pay"}"
}
