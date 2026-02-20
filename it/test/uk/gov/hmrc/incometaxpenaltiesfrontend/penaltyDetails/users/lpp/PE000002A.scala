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

  override val nino: String = "PE000002A"
  override val expectedNumberOfLPPPenaltyCards: Int = 2
  override val expectedNumberOfLSPPenaltyCards: Int = 1
  override val numberOfLSPPenalties: Int = 1

  def penaltyCard0ExpectedContent(card: Element): Unit = {
    validatePenaltyCardTitle(card, expectedTitle = "Second late payment penalty: £46.02")
    validateCardTag(card, expectedTag = "Estimate")
    val cardRows = getCardsRows(card)
    cardRows.size() shouldBe 3
    validateSummary(cardRows.get(0), "Overdue charge", "Income Tax for 2027 to 2028 tax year")
    validateSummary(cardRows.get(1), "Income Tax due", "31 January 2029")
    validateSummary(cardRows.get(2), "Income Tax paid", "Payment not yet received")
    validateViewCalculationLink(card, 0, isSecondLPP = true)
    validateAppealLink(card.getElementsByClass("govuk-link").get(1))
  }

  def penaltyCard1ExpectedContent(card: Element): Unit = {
    validatePenaltyCardTitle(card, expectedTitle = "First late payment penalty: £46.02")
    validateCardTag(card, expectedTag = "Due")
    val cardRows = getCardsRows(card)
    cardRows.size() shouldBe 4
    validateSummary(cardRows.get(0), "Pay penalty by", "18 April 2029")
    validateSummary(cardRows.get(1), "Overdue charge", "Income Tax for 2027 to 2028 tax year")
    validateSummary(cardRows.get(2), "Income Tax due", "31 January 2029")
    validateSummary(cardRows.get(3), "Income Tax paid", "17 March 2029")
    validateViewCalculationLink(card, 1)
    validateAppealLink(card.getElementsByClass("govuk-link").get(1))
  }
  
  override val expectedPenaltyCardsContent: Map[Int, Element => Unit] = Map(
    0 -> penaltyCard0ExpectedContent,
    1 -> penaltyCard1ExpectedContent
  )

  override val expectedOverviewText: Boolean => String = isAgent =>
    s"Overview ${if (isAgent) "Your client’s" else "Your"} account has: overdue Income Tax charges late payment penalties 1 late submission penalty point Check what you owe"
}
