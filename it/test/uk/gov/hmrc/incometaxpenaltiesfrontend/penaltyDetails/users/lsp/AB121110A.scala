/*
 * Copyright 2026 HM Revenue & Customs
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

object AB121110A extends UserDetailsData {

  override val nino: String = "AB121110A"
  override val hasFinanicalLSP: Boolean = false
  override val numberOfFinancialPenalties: Int = 0
  override val numberOfLSPPenalties: Int = 1

  override val expectedNumberOfLSPPenaltyCards: Int = 1
  override val expectedNumberOfLPPPenaltyCards: Int = 0

  def penaltyCard0ExpectedContent(card: Element): Unit = {
    validatePenaltyCardTitle(card, expectedTitle = "Penalty point 1: Late tax return")
    validateCardTag(card, expectedTag = "Active")
    val cardRows = getCardsRows(card)
    cardRows.size() shouldBe 4
    validateSummary(cardRows.get(0), "Tax year", "2026 to 2027")
    validateSummary(cardRows.get(1), "Return due", "31 January 2028")
    validateSummary(cardRows.get(2), "Return submitted", "Not yet received")
    validateSummary(cardRows.get(3), "Point due to expire", "5 April 2029")
  }

  override val expectedPenaltyCardsContent: Map[Int, Element => Unit] = Map(
    0 -> penaltyCard0ExpectedContent
  )

  override val expectedOverviewText: Boolean => String = isAgent =>
    s"Overview Your${if (isAgent) " client’s" else ""} account has 1 late submission penalty point"
}




