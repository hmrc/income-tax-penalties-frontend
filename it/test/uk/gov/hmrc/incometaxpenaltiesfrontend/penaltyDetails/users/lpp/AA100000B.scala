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

object AA100000B extends UserDetailsData {

  override val nino: String = "AA100000B"
  override val hasFinancialLSP: Boolean = true
  override val numberOfLSPPenalties: Int = 0

  override val expectedNumberOfLSPPenaltyCards: Int = 1
  override val expectedNumberOfLPPPenaltyCards: Int = 0

  def penaltyCard0ExpectedContent(card: Element): Unit = {
    validatePenaltyCardTitle(card, expectedTitle = "First late payment penalty: £40.00")
    validateCardTag(card, expectedTag = "Due")
    val cardRows = getCardsRows(card)
    cardRows.size() shouldBe 5
    validateSummary(cardRows.get(0), "Missing or late income sources", "JB Painting and Decorating UK property rental income")
    validateSummary(cardRows.get(1), "Update period", "6 April 2027 to 5 July 2027")
    validateSummary(cardRows.get(2), "Update due", "7 August 2027")
    validateSummary(cardRows.get(3), "Update submitted", "1 September 2027")
    validateSummary(cardRows.get(4), "Appeal status", "Appeal successful")
  }

  override val expectedPenaltyCardsContent: Map[Int, Element => Unit] = Map(
    0 -> penaltyCard0ExpectedContent
  )

  override val expectedOverviewText: Boolean => String = isAgent =>
    s"Overview ${if (isAgent) "Your client’s" else "Your"} account has a late payment penalty"
}
