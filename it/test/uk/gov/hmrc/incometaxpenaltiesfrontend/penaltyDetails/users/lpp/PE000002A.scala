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

object PE000002A extends UserDetailsData {

  override val nino: String = "AL300002A"
  override val expectedNumberOfLPPPenaltyCards: Int = 6
  override val expectedNumberOfLSPPenaltyCards: Int = 0

  def penaltyCard0ExpectedContent(card: Element): Unit = {
    validatePenaltyCardTitle(card, expectedTitle = "Second late payment penalty: £40.00")
    validateCardTag(card, expectedTag = "Paid")
    val cardRows = getCardsRows(card)
    cardRows.size() shouldBe 4
    validateSummary(cardRows.get(0), "Overdue charge", "Income Tax for 2026 to 2027 tax year")
    validateSummary(cardRows.get(1), "Income Tax due", "31 January 2028")
    validateSummary(cardRows.get(2), "Income Tax paid", "16 March 2028")
    validateSummary(cardRows.get(3), "Appeal status", "Decision upheld")
    validateViewCalculationLink(card, 0, isSecondLPP = true)
  }

  def penaltyCard1ExpectedContent(card: Element): Unit = {
    validatePenaltyCardTitle(card, expectedTitle = "First late payment penalty: £40.00")
    validateCardTag(card, expectedTag = "Paid")
    val cardRows = getCardsRows(card)
    cardRows.size() shouldBe 4
    validateSummary(cardRows.get(0), "Overdue charge", "Income Tax for 2026 to 2027 tax year")
    validateSummary(cardRows.get(1), "Income Tax due", "31 January 2028")
    validateSummary(cardRows.get(2), "Income Tax paid", "16 March 2028")
    validateSummary(cardRows.get(3), "Appeal status", "Decision upheld")
    validateViewCalculationLink(card, 1)
  }

  def penaltyCard2ExpectedContent(card: Element): Unit = {
    validatePenaltyCardTitle(card, expectedTitle = "Second late payment penalty: £40.00")
    validateCardTag(card, expectedTag = "Overdue")
    val cardRows = getCardsRows(card)
    cardRows.size() shouldBe 5
    validateSummary(cardRows.get(0), "Pay penalty by", "18 April 2027")
    validateSummary(cardRows.get(1), "Overdue charge", "Income Tax for 2025 to 2026 tax year")
    validateSummary(cardRows.get(2), "Income Tax due", "31 January 2027")
    validateSummary(cardRows.get(3), "Income Tax paid", "17 March 2027")
    validateSummary(cardRows.get(4), "Appeal status", "Decision upheld")
    validateViewCalculationLink(card, 2, isSecondLPP = true)
  }

  def penaltyCard3ExpectedContent(card: Element): Unit = {
    validatePenaltyCardTitle(card, expectedTitle = "First late payment penalty: £40.00")
    validateCardTag(card, expectedTag = "Overdue")
    val cardRows = getCardsRows(card)
    cardRows.size() shouldBe 5
    validateSummary(cardRows.get(0), "Pay penalty by", "17 March 2027")
    validateSummary(cardRows.get(1), "Overdue charge", "Income Tax for 2025 to 2026 tax year")
    validateSummary(cardRows.get(2), "Income Tax due", "31 January 2027")
    validateSummary(cardRows.get(3), "Income Tax paid", "Payment not yet received")
    validateSummary(cardRows.get(4), "Appeal status", "Decision upheld")
    validateViewCalculationLink(card, 3)
  }

  def penaltyCard4ExpectedContent(card: Element): Unit = {
    validatePenaltyCardTitle(card, expectedTitle = "Second late payment penalty: £40.00")
    validateCardTag(card, expectedTag = "Overdue")
    val cardRows = getCardsRows(card)
    cardRows.size() shouldBe 5
    validateSummary(cardRows.get(0), "Pay penalty by", "18 April 2026")
    validateSummary(cardRows.get(1), "Overdue charge", "Income Tax for 2024 to 2025 tax year")
    validateSummary(cardRows.get(2), "Income Tax due", "31 January 2026")
    validateSummary(cardRows.get(3), "Income Tax paid", "17 March 2026")
    validateSummary(cardRows.get(4), "Appeal status", "Appeal rejected")
    validateViewCalculationLink(card, 4, isSecondLPP = true)
    validateAppealLink(card.getElementsByClass("govuk-link").get(1), is2ndStage = true)
  }

  def penaltyCard5ExpectedContent(card: Element): Unit = {
    validatePenaltyCardTitle(card, expectedTitle = "First late payment penalty: £40.00")
    validateCardTag(card, expectedTag = "Overdue")
    val cardRows = getCardsRows(card)
    cardRows.size() shouldBe 5
    validateSummary(cardRows.get(0), "Pay penalty by", "17 March 2026")
    validateSummary(cardRows.get(1), "Overdue charge", "Income Tax for 2024 to 2025 tax year")
    validateSummary(cardRows.get(2), "Income Tax due", "31 January 2026")
    validateSummary(cardRows.get(3), "Income Tax paid", "Payment not yet received")
    validateSummary(cardRows.get(4), "Appeal status", "Appeal rejected")
    validateViewCalculationLink(card, 5)
    validateAppealLink(card.getElementsByClass("govuk-link").get(1), is2ndStage = true)
  }
  
  override val expectedPenaltyCardsContent: Map[Int, Element => Unit] = Map(
    0 -> penaltyCard0ExpectedContent,
    1 -> penaltyCard1ExpectedContent,
    2 -> penaltyCard2ExpectedContent,
    3 -> penaltyCard3ExpectedContent,
    4 -> penaltyCard4ExpectedContent,
    5 -> penaltyCard5ExpectedContent
  )

  override val expectedOverviewText: Boolean => String = isAgent =>
    s"Overview ${if (isAgent) "Your client’s" else "Your"} account has late payment penalties Check amounts${if(isAgent) "" else " and pay"}"

  override val timeMachineDate: Option[String] = Some("05/03/2028")
}
