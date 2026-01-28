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

package uk.gov.hmrc.incometaxpenaltiesfrontend.penaltyDetails.users.lpp

import org.jsoup.nodes.Element
import uk.gov.hmrc.incometaxpenaltiesfrontend.penaltyDetails.users.UserDetailsData

object AL300001A extends UserDetailsData {

  override val nino: String = "AL300001A"
  override val expectedNumberOfLPPPenaltyCards: Int = 6
  override val expectedNumberOfLSPPenaltyCards: Int = 0

  def penaltyCard0ExpectedContent(card: Element): Unit = {
    validatePenaltyCardTitle(card, expectedTitle = "Second late payment penalty: £40.00")
    validateCardTag(card, expectedTag = "Cancelled")
    val cardRows = getCardsRows(card)
    cardRows.size() shouldBe 4
    validateSummary(cardRows.get(0), "Overdue charge", "Income Tax for 2026 to 2027 tax year")
    validateSummary(cardRows.get(1), "Income Tax due", "31 January 2028")
    validateSummary(cardRows.get(2), "Income Tax paid", "16 March 2028")
    validateSummary(cardRows.get(3), "Appeal status", "Appeal successful")
  }

  def penaltyCard1ExpectedContent(card: Element): Unit = {
    validatePenaltyCardTitle(card, expectedTitle = "First late payment penalty: £40.00")
    validateCardTag(card, expectedTag = "Cancelled")
    val cardRows = getCardsRows(card)
    cardRows.size() shouldBe 4
    validateSummary(cardRows.get(0), "Overdue charge", "Income Tax for 2026 to 2027 tax year")
    validateSummary(cardRows.get(1), "Income Tax due", "31 January 2028")
    validateSummary(cardRows.get(2), "Income Tax paid", "16 March 2028")
    validateSummary(cardRows.get(3), "Appeal status", "Appeal successful")
  }

  def penaltyCard2ExpectedContent(card: Element): Unit = {
    validatePenaltyCardTitle(card, expectedTitle = "Second late payment penalty: £40.00")
    validateCardTag(card, expectedTag = "Cancelled")
    val cardRows = getCardsRows(card)
    cardRows.size() shouldBe 4
    validateSummary(cardRows.get(0), "Overdue charge", "Income Tax for 2025 to 2026 tax year")
    validateSummary(cardRows.get(1), "Income Tax due", "31 January 2027")
    validateSummary(cardRows.get(2), "Income Tax paid", "16 March 2028")
    validateSummary(cardRows.get(3), "Appeal status", "Appeal successful")
  }

  def penaltyCard3ExpectedContent(card: Element): Unit = {
    validatePenaltyCardTitle(card, expectedTitle = "First late payment penalty: £40.00")
    validateCardTag(card, expectedTag = "Cancelled")
    val cardRows = getCardsRows(card)
    cardRows.size() shouldBe 4
    validateSummary(cardRows.get(0), "Overdue charge", "Income Tax for 2025 to 2026 tax year")
    validateSummary(cardRows.get(1), "Income Tax due", "31 January 2027")
    validateSummary(cardRows.get(2), "Income Tax paid", "Payment not yet received")
    validateSummary(cardRows.get(3), "Appeal status", "Appeal successful")
  }

  def penaltyCard4ExpectedContent(card: Element): Unit = {
    validatePenaltyCardTitle(card, expectedTitle = "Second late payment penalty: £40.00")
    validateCardTag(card, expectedTag = "Cancelled")
    val cardRows = getCardsRows(card)
    cardRows.size() shouldBe 4
    validateSummary(cardRows.get(0), "Overdue charge", "Income Tax for 2024 to 2025 tax year")
    validateSummary(cardRows.get(1), "Income Tax due", "31 January 2026")
    validateSummary(cardRows.get(2), "Income Tax paid", "16 March 2028")
    validateSummary(cardRows.get(3), "Appeal status", "Appeal successful")
  }

  def penaltyCard5ExpectedContent(card: Element): Unit = {
    validatePenaltyCardTitle(card, expectedTitle = "First late payment penalty: £40.00")
    validateCardTag(card, expectedTag = "Cancelled")
    val cardRows = getCardsRows(card)
    cardRows.size() shouldBe 4
    validateSummary(cardRows.get(0), "Overdue charge", "Income Tax for 2024 to 2025 tax year")
    validateSummary(cardRows.get(1), "Income Tax due", "31 January 2026")
    validateSummary(cardRows.get(2), "Income Tax paid", "Payment not yet received")
    validateSummary(cardRows.get(3), "Appeal status", "Appeal successful")
  }
  
  override val expectedPenaltyCardsContent: Map[Int, Element => Unit] = Map(
    0 -> penaltyCard0ExpectedContent,
    1 -> penaltyCard1ExpectedContent,
    2 -> penaltyCard2ExpectedContent,
    3 -> penaltyCard3ExpectedContent,
    4 -> penaltyCard4ExpectedContent,
    5 -> penaltyCard5ExpectedContent
  )

  override val expectedOverviewText: Boolean => String = _ => ""
}
