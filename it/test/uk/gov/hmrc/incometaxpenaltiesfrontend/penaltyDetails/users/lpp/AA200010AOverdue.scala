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
import uk.gov.hmrc.incometaxpenaltiesfrontend.penaltyDetails.users.lpp.AA123450A.validateViewCalculationLink

object AA200010AOverdue extends UserDetailsData {

  override val nino: String = AA200010A.nino
  override val numberOfLPPPenalties: Int = 0
  override val expectedNumberOfLSPPenaltyCards: Int = AA200010A.expectedNumberOfLSPPenaltyCards
  override val expectedNumberOfLPPPenaltyCards: Int = AA200010A.expectedNumberOfLPPPenaltyCards

  def penaltyCard1ExpectedContent(card: Element): Unit = {
    validatePenaltyCardTitle(card, expectedTitle = "First late payment penalty: £80.00")
    validateCardTag(card, expectedTag = "Overdue")
    val cardRows = getCardsRows(card)
    cardRows.size() shouldBe 5
    validateSummary(cardRows.get(0), "Pay penalty by", "16 March 2028")
    validateSummary(cardRows.get(1), "Overdue charge", "Income Tax for 2026 to 2027 tax year")
    validateSummary(cardRows.get(2), "Income Tax due", "31 January 2028")
    validateSummary(cardRows.get(3), "Income Tax paid", "Payment not yet received")
    validateSummary(cardRows.get(4), "Appeal status", "Appeal rejected")
    validateViewCalculationLink(card, 1)
    validateAppealLink(card.getElementsByClass("govuk-link").get(1), is2ndStage = true)
  }
  override val expectedPenaltyCardsContent: Map[Int, Element => Unit] = Map(
    0 -> AA200010A.penaltyCard0ExpectedContent,
    1 -> penaltyCard1ExpectedContent
  )

  override val expectedOverviewText: Boolean => String = AA200010A.expectedOverviewText
  override val timeMachineDate: Option[String] = Some("20/05/2028")
}
