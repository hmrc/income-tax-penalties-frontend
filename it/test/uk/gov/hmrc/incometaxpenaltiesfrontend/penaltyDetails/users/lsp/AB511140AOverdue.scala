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

object AB511140AOverdue extends UserDetailsData {

  override val nino: String = AB511140A.nino
  override val hasFinancialLSP: Boolean = AB511140A.hasFinancialLSP
  override val numberOfLSPPenalties: Int = AB511140A.numberOfLSPPenalties
  override val numberOfPaidFinancialPenalties: Int = AB511140A.numberOfPaidFinancialPenalties
  override val numberOfUnpaidFinancialPenalties: Int = AB511140A.numberOfUnpaidFinancialPenalties

  override val expectedNumberOfLSPPenaltyCards: Int = AB511140A.expectedNumberOfLSPPenaltyCards
  override val expectedNumberOfLPPPenaltyCards: Int = AB511140A.expectedNumberOfLPPPenaltyCards

  def penaltyCard0ExpectedContent(card: Element): Unit = {
    validatePenaltyCardTitle(card, expectedTitle = "Additional £200 penalty: Late tax return")
    validateCardTag(card, expectedTag = "Overdue")
    val cardRows = getCardsRows(card)
    cardRows.size() shouldBe 4
    validateSummary(cardRows.get(0), "Pay penalty by", "16 March 2028")
    validateSummary(cardRows.get(1), "Tax year", "2026 to 2027")
    validateSummary(cardRows.get(2), "Return due", "31 January 2028")
    validateSummary(cardRows.get(3), "Return submitted", "Not yet received")
    validateAppealLink(card.getElementsByClass("govuk-link").first())
  }

  override val expectedPenaltyCardsContent: Map[Int, Element => Unit] = Map(
    0 -> penaltyCard0ExpectedContent,
    1 -> AB511140A.penaltyCard1ExpectedContent,
    2 -> AB511140A.penaltyCard2ExpectedContent,
    3 -> AB511140A.penaltyCard3ExpectedContent,
    4 -> AB511140A.penaltyCard4ExpectedContent,
    5 -> AB511140A.penaltyCard5ExpectedContent
  )

  override val expectedOverviewText: Boolean => String = AB511140A.expectedOverviewText

  override val timeMachineDate: Option[String] = Some("04/04/2028")
}
