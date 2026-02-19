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
    0 -> AA200000B.penaltyCard0ExpectedContent,
    1 -> AA200000B.penaltyCard1ExpectedContent
  )

  override val expectedOverviewText: Boolean => String = AA200000B.expectedOverviewText

  override val timeMachineDate: Option[String] = Some("20/05/2028")
}
