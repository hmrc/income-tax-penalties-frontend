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

object AA200000BOverdue extends UserDetailsData {

  override val nino: String = AA200000B.nino
  override val expectedNumberOfLSPPenaltyCards: Int = AA200000B.expectedNumberOfLSPPenaltyCards
  override val expectedNumberOfLPPPenaltyCards: Int = AA200000B.expectedNumberOfLPPPenaltyCards

  override val expectedPenaltyCardsContent: Map[Int, Element => Unit] = Map(
    0 -> ((card: Element) => {
      validatePenaltyCardTitle(card, expectedTitle = "Second late payment penalty: £15.89")
      validateCardTag(card, expectedTag = "Estimate")
      val cardRows = getCardsRows(card)
      cardRows.size() shouldBe 3
      validateSummary(cardRows.get(0), "Overdue charge", "Income Tax for 2026 to 2027 tax year")
      validateSummary(cardRows.get(1), "Income Tax due", "31 January 2028")
      validateSummary(cardRows.get(2), "Income Tax paid", "Payment not yet received")
      validateViewCalculationLink(card, 0, isSecondLPP = true)
    }),
    1 -> ((card: Element) => {
      validatePenaltyCardTitle(card, expectedTitle = "First late payment penalty: £120.00")
      validateCardTag(card, expectedTag = "Overdue")
      val cardRows = getCardsRows(card)
      cardRows.size() shouldBe 4
      validateSummary(cardRows.get(0), "Pay penalty by", "3 May 2028")
      validateSummary(cardRows.get(1), "Overdue charge", "Income Tax for 2026 to 2027 tax year")
      validateSummary(cardRows.get(2), "Income Tax due", "31 January 2028")
      validateSummary(cardRows.get(3), "Income Tax paid", "Payment not yet received")
      validateViewCalculationLink(card, 1)
      validateAppealLink(card.getElementsByClass("govuk-link").get(1))
    })
  )

  override val expectedOverviewText: String = AA200000B.expectedOverviewText
  override val timeMachineDate: String = "20/05/2028"
}
