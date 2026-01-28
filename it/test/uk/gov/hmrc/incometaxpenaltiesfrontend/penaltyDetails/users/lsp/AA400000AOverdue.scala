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

object AA400000AOverdue extends UserDetailsData {

  override val nino: String = AA400000A.nino
  override val hasFinancialLSP: Boolean = AA400000A.hasFinancialLSP
  override val numberOfLSPPenalties: Int = AA400000A.numberOfLSPPenalties
  override val numberOfUnpaidFinancialPenalties: Int = AA400000A.numberOfUnpaidFinancialPenalties

  override val expectedNumberOfLSPPenaltyCards: Int = AA400000A.expectedNumberOfLSPPenaltyCards
  override val expectedNumberOfLPPPenaltyCards: Int = AA400000A.expectedNumberOfLPPPenaltyCards

  def penaltyCard0ExpectedContent(card: Element): Unit = {
    validatePenaltyCardTitle(card, expectedTitle = "Penalty point 4: Late tax return - £200 penalty")
    validateCardTag(card, expectedTag = "Overdue")
    val cardRows = getCardsRows(card)
    cardRows.size() shouldBe 5
    validateSummary(cardRows.get(0), "Pay penalty by", "16 March 2028")
    validateSummary(cardRows.get(1), "Tax year", "2026 to 2027")
    validateSummary(cardRows.get(2), "Return due", "31 January 2028")
    validateSummary(cardRows.get(3), "Return submitted", "23 February 2028")
    validateSummary(cardRows.get(4), "Appeal status", "Appeal in progress")
  }

  override val expectedPenaltyCardsContent: Map[Int, Element => Unit] = Map(
    0 -> penaltyCard0ExpectedContent,
    1 -> AA400000A.penaltyCard1ExpectedContent,
    2 -> AA400000A.penaltyCard2ExpectedContent,
    3 -> AA400000A.penaltyCard3ExpectedContent
  )

  override val expectedOverviewText: Boolean => String = AA400000A.expectedOverviewText

  override val timeMachineDate: Option[String] = Some("30/03/2028")
}

