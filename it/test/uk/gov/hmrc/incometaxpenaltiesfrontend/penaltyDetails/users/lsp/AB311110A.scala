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

object AB311110A extends UserDetailsData {

  override val nino: String = "AB311110A"
  override val hasFinancialLSP: Boolean = false
  override val numberOfLSPPenalties: Int = 3

  override val expectedNumberOfLSPPenaltyCards: Int = 3
  override val expectedNumberOfLPPPenaltyCards: Int = 0

  def penaltyCard0ExpectedContent(card: Element): Unit = {
    validatePenaltyCardTitle(card, expectedTitle = "Penalty point 3: Late update")
    validateCardTag(card, expectedTag = "Active")
    val cardRows = getCardsRows(card)
    cardRows.size() shouldBe 5
    validateSummary(cardRows.get(0), "Missing or late income sources", "JB Painting and Decorating UK property rental income")
    validateSummary(cardRows.get(1), "Update period", "6 January 2027 to 5 April 2027")
    validateSummary(cardRows.get(2), "Update due", "7 May 2027")
    validateSummary(cardRows.get(3), "Update submitted", "Not yet received")
    validateSummary(cardRows.get(4), "Point due to expire", "5 April 2029")
    validateAppealLink(card.getElementsByClass("govuk-link").first())
  }

  def penaltyCard1ExpectedContent(card: Element): Unit = {
    validatePenaltyCardTitle(card, expectedTitle = "Penalty point 2: Late update")
    validateCardTag(card, expectedTag = "Active")
    val cardRows = getCardsRows(card)
    cardRows.size() shouldBe 5
    validateSummary(cardRows.get(0), "Missing or late income sources", "JB Painting and Decorating UK property rental income")
    validateSummary(cardRows.get(1), "Update period", "6 October 2026 to 5 January 2027")
    validateSummary(cardRows.get(2), "Update due", "7 February 2027")
    validateSummary(cardRows.get(3), "Update submitted", "1 March 2027")
    validateSummary(cardRows.get(4), "Point due to expire", "5 January 2029")
    validateAppealLink(card.getElementsByClass("govuk-link").first())
  }

  def penaltyCard2ExpectedContent(card: Element): Unit = {
    validatePenaltyCardTitle(card, expectedTitle = "Penalty point 1: Late tax return")
    validateCardTag(card, expectedTag = "Active")
    val cardRows = getCardsRows(card)
    cardRows.size() shouldBe 5
    validateSummary(cardRows.get(0), "Tax year", "2025 to 2026")
    validateSummary(cardRows.get(1), "Return due", "31 January 2027")
    validateSummary(cardRows.get(2), "Return submitted", "22 February 2027")
    validateSummary(cardRows.get(3), "Point due to expire", "5 April 2028")
    validateSummary(cardRows.get(4), "Appeal status", "Decision upheld")
  }

  override val expectedPenaltyCardsContent: Map[Int, Element => Unit] = Map(
    0 -> penaltyCard0ExpectedContent,
    1 -> penaltyCard1ExpectedContent,
    2 -> penaltyCard2ExpectedContent
  )

  override val expectedOverviewText: Boolean => String = isAgent =>
    s"Overview Your${if (isAgent) " client’s" else ""} account has 3 late submission penalty points"
}
