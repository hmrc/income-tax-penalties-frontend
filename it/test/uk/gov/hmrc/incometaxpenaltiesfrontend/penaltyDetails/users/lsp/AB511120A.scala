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
import uk.gov.hmrc.incometaxpenaltiesfrontend.penaltyDetails.users.lpp.AL300003A.{getCardsRows, validateAppealLink, validateCardTag, validatePenaltyCardTitle, validateSummary}

object AB511120A extends UserDetailsData {

  override val nino: String = "AB511120A"
  override val hasFinanicalLSP: Boolean = true
  override val hasPaidAllFinancialLSP: Boolean = true
  override val numberOfLSPPenalties: Int = 5

  override val expectedNumberOfLSPPenaltyCards: Int = 5
  override val expectedNumberOfLPPPenaltyCards: Int = 0

  def penaltyCard0ExpectedContent(card: Element): Unit = {
    validatePenaltyCardTitle(card, expectedTitle = "Additional £200 penalty: Late update")
    validateCardTag(card, expectedTag = "Paid")
    val cardRows = getCardsRows(card)
    cardRows.size() shouldBe 6
    validateSummary(cardRows.get(0), "Missing or late income sources", "JB Painting and Decorating UK property rental income")
    validateSummary(cardRows.get(1), "Pay penalty by", "21 June 2027")
    validateSummary(cardRows.get(2), "Update period", "6 January 2027 to 5 April 2027")
    validateSummary(cardRows.get(3), "Update due", "7 May 2027")
    validateSummary(cardRows.get(4), "Update submitted", "1 June 2027")
    validateSummary(cardRows.get(5), "Appeal status", "Decision upheld")
  }

  def penaltyCard1ExpectedContent(card: Element): Unit = {
    validatePenaltyCardTitle(card, expectedTitle = "Penalty point 4: Late update - £200 penalty")
    validateCardTag(card, expectedTag = "Paid")
    val cardRows = getCardsRows(card)
    cardRows.size() shouldBe 5
    validateSummary(cardRows.get(0), "Missing or late income sources", "JB Painting and Decorating UK property rental income")
    validateSummary(cardRows.get(1), "Pay penalty by", "24 March 2027")
    validateSummary(cardRows.get(2), "Update period", "6 October 2026 to 5 January 2027")
    validateSummary(cardRows.get(3), "Update due", "7 February 2027")
    validateSummary(cardRows.get(4), "Update submitted", "1 March 2027")
    validateAppealLink(card.getElementsByClass("govuk-link").first())
  }

  def penaltyCard2ExpectedContent(card: Element): Unit = {
    validatePenaltyCardTitle(card, expectedTitle = "Penalty point 3: Late tax return")
    validateCardTag(card, expectedTag = "Active")
    val cardRows = getCardsRows(card)
    cardRows.size() shouldBe 3
    validateSummary(cardRows.get(0), "Tax year", "2025 to 2026")
    validateSummary(cardRows.get(1), "Return due", "31 January 2027")
    validateSummary(cardRows.get(2), "Return submitted", "22 February 2027")
    validateAppealLink(card.getElementsByClass("govuk-link").first())
  }

  def penaltyCard3ExpectedContent(card: Element): Unit = {
    validatePenaltyCardTitle(card, expectedTitle = "Penalty point 2: Late update")
    validateCardTag(card, expectedTag = "Active")
    val cardRows = getCardsRows(card)
    cardRows.size() shouldBe 4
    validateSummary(cardRows.get(0), "Missing or late income sources", "JB Painting and Decorating UK property rental income")
    validateSummary(cardRows.get(1), "Update period", "6 July 2026 to 5 October 2026")
    validateSummary(cardRows.get(2), "Update due", "7 November 2026")
    validateSummary(cardRows.get(3), "Update submitted", "1 December 2026")
    validateAppealLink(card.getElementsByClass("govuk-link").first())
  }


  def penaltyCard4ExpectedContent(card: Element): Unit = {
    validatePenaltyCardTitle(card, expectedTitle = "Penalty point 1: Late update")
    validateCardTag(card, expectedTag = "Active")
    val cardRows = getCardsRows(card)
    cardRows.size() shouldBe 4
    validateSummary(cardRows.get(0), "Missing or late income sources", "JB Painting and Decorating UK property rental income")
    validateSummary(cardRows.get(1), "Update period", "6 April 2026 to 5 July 2026")
    validateSummary(cardRows.get(2), "Update due", "7 August 2026")
    validateSummary(cardRows.get(3), "Update submitted", "1 September 2026")
    validateAppealLink(card.getElementsByClass("govuk-link").first())
  }

  override val expectedPenaltyCardsContent: Map[Int, Element => Unit] = Map(
    0 -> penaltyCard0ExpectedContent,
    1 -> penaltyCard1ExpectedContent,
    2 -> penaltyCard2ExpectedContent,
    3 -> penaltyCard3ExpectedContent,
    4 -> penaltyCard4ExpectedContent
  )

  override val expectedOverviewText: Boolean => String = isAgent =>
    s"Overview Your${if (isAgent) " client’s" else ""} account has the maximum number of late submission penalty points"
}
