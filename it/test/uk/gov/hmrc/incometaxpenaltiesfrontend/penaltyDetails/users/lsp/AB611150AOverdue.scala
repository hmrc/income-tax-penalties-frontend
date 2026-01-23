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

object AB611150AOverdue extends UserDetailsData {

  override val nino: String = AB611150A.nino
  override val hasFinancialLSP: Boolean = AB611150A.hasFinancialLSP
  override val numberOfLSPPenalties: Int = AB611150A.numberOfLSPPenalties
  override val numberOfPaidFinancialPenalties: Int = AB611150A.numberOfPaidFinancialPenalties
  override val numberOfUnpaidFinancialPenalties: Int = AB611150A.numberOfUnpaidFinancialPenalties

  override val expectedNumberOfLSPPenaltyCards: Int = AB611150A.expectedNumberOfLSPPenaltyCards
  override val expectedNumberOfLPPPenaltyCards: Int = AB611150A.expectedNumberOfLPPPenaltyCards

  def penaltyCard0ExpectedContent(card: Element): Unit = {
    validatePenaltyCardTitle(card, expectedTitle = "Additional £200 penalty: Late update")
    validateCardTag(card, expectedTag = "Overdue")
    val cardRows = getCardsRows(card)
    cardRows.size() shouldBe 5
    validateSummary(cardRows.get(0), "Missing or late income sources", "JB Painting and Decorating UK property rental income")
    validateSummary(cardRows.get(1), "Pay penalty by", "23 March 2028")
    validateSummary(cardRows.get(2), "Update period", "6 October 2027 to 5 January 2028")
    validateSummary(cardRows.get(3), "Update due", "7 February 2028")
    validateSummary(cardRows.get(4), "Update submitted", "Not yet received")
    validateAppealLink(card.getElementsByClass("govuk-link").first())
  }

  override val expectedPenaltyCardsContent: Map[Int, Element => Unit] = Map(
    0 -> penaltyCard0ExpectedContent,
    1 -> AB611150A.penaltyCard1ExpectedContent,
    2 -> AB611150A.penaltyCard2ExpectedContent,
    3 -> AB611150A.penaltyCard3ExpectedContent,
    4 -> AB611150A.penaltyCard4ExpectedContent,
    5 -> AB611150A.penaltyCard5ExpectedContent
  )

  override val expectedOverviewText: Boolean => String = AB611150A.expectedOverviewText

  override val timeMachineDate: Option[String] = Some("30/04/2028")
}
