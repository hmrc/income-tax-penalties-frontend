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

object AC100000B extends UserDetailsData {

  override val nino: String = "AC100000B"
  override val expectedNumberOfLPPPenaltyCards: Int = 1
  override val expectedNumberOfLSPPenaltyCards: Int = 0

  def penaltyCard0ExpectedContent(card: Element): Unit = {
    validatePenaltyCardTitle(card, expectedTitle = "First late payment penalty: £40.00")
    validateCardTag(card, expectedTag = "Estimate")
    val cardRows = getCardsRows(card)
    cardRows.size() shouldBe 3
    validateSummary(cardRows.get(0), "Overdue charge", "Income Tax for 2024 to 2025 tax year")
    validateSummary(cardRows.get(1), "Income Tax due", "31 January 2026")
    validateSummary(cardRows.get(2), "Income Tax paid", "Payment plan proposed")
    validateViewCalculationLink(card, 0)
  }
  
  override val expectedPenaltyCardsContent: Map[Int, Element => Unit] = Map(
    0 -> penaltyCard0ExpectedContent
  )

  override val expectedOverviewText: Boolean => String = isAgent =>
    s"Overview ${if (isAgent) "Your client’s" else "Your"} account has: overdue Income Tax charges unpaid interest a late payment penalty Check amounts${if (isAgent) "" else " and pay"}"
}
