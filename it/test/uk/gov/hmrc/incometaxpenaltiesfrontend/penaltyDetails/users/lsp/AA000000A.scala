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

object AA000000A extends UserDetailsData {

  override val nino: String = "AA000000A"
  override val hasFinancialLSP: Boolean = true
  override val numberOfLSPPenalties: Int = 0

  override val expectedNumberOfLSPPenaltyCards: Int = 0
  override val expectedNumberOfLPPPenaltyCards: Int = 0

  

  override val expectedPenaltyCardsContent: Map[Int, Element => Unit] = Map.empty

  override val expectedOverviewText: Boolean => String = isAgent =>
    s"Overview Your account has: late submission penalties the maximum number of late submission penalty points Check amounts${if(isAgent) "" else " and pay"}"
}
