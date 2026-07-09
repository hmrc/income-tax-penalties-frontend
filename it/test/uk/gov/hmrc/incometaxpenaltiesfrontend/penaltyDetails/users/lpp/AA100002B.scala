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
import uk.gov.hmrc.incometaxpenaltiesfrontend.penaltyDetails.users.lpp.AA244440A.validateAppealLink

object AA100002B extends UserDetailsData {

  override val nino: String = "AA100002B"
  override val expectedNumberOfLSPPenaltyCards: Int = 0
  override val expectedNumberOfLPPPenaltyCards: Int = 3

  def penaltyCard0ExpectedContent(card: Element): Unit = {
    validatePenaltyCardTitle(card, expectedTitle = "Second late payment penalty: £8.21")
    validateCardTag(card, expectedTag = "Paid")
    val cardRows = getCardsRows(card)
    cardRows.size() shouldBe 3
    validateSummary(cardRows.get(0), "Overdue charge", "Income Tax for 2023 to 2024 tax year")
    validateSummary(cardRows.get(1), "Income Tax due", "31 January 2025")
    validateSummary(cardRows.get(2), "Income Tax paid", "17 March 2025")
    validateViewCalculationLink(card, 0, isSecondLPP = true)
    validateAppealLink(card.getElementsByClass("govuk-link").get(1))
  }

  def penaltyCard1ExpectedContent(card: Element): Unit = {
    validatePenaltyCardTitle(card, expectedTitle = "First late payment penalty: £115.00")
    validateCardTag(card, expectedTag = "Paid")
    val cardRows = getCardsRows(card)
    cardRows.size() shouldBe 3
    validateSummary(cardRows.get(0), "Overdue charge", "Income Tax for 2023 to 2024 tax year")
    validateSummary(cardRows.get(1), "Income Tax due", "31 January 2025")
    validateSummary(cardRows.get(2), "Income Tax paid", "17 March 2025")
    validateViewCalculationLink(card, 1)
    validateAppealLink(card.getElementsByClass("govuk-link").get(1))
  }

  def penaltyCard2ExpectedContent(card: Element): Unit = {
    validatePenaltyCardTitle(card, expectedTitle = "Additional first late payment penalty: £15.00")
    validateCardTag(card, expectedTag = "Paid")
    val cardRows = getCardsRows(card)
    cardRows.size() shouldBe 3
    validateSummary(cardRows.get(0), "Overdue charge", "Income Tax for 2023 to 2024 tax year")
    validateSummary(cardRows.get(1), "Income Tax due", "31 January 2025")
    validateSummary(cardRows.get(2), "Income Tax paid", "17 March 2025")
    validateViewCalculationLink(card, 2)
    validateAppealLink(card.getElementsByClass("govuk-link").get(1))
  }

  override val expectedPenaltyCardsContent: Map[Int, Element => Unit] = Map(
    0 -> penaltyCard0ExpectedContent,
    1 -> penaltyCard1ExpectedContent,
    2 -> penaltyCard2ExpectedContent
  )

  override val expectedOverviewText: String = ""

  override val timeMachineDate: String = "25/04/2025"
}

