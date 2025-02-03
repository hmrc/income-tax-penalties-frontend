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

package uk.gov.hmrc.incometaxpenaltiesfrontend.viewModels

import play.api.i18n.Messages
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.PenaltyDetails
import uk.gov.hmrc.incometaxpenaltiesfrontend.views.ViewUtils.pluralOrSingular

case class PenaltiesOverviewViewModel(content: Seq[String], hasFinancialCharge: Boolean)

object PenaltiesOverviewViewModel {

  def apply(penaltyDetails: PenaltyDetails)(implicit messages: Messages): PenaltiesOverviewViewModel = {
    import penaltyDetails._
    PenaltiesOverviewViewModel(
      content = Seq(
        Option.when(unpaidIncomeTax > 0)(
          messages("overview.unpaidReturnCharges.bullet")
        ),
        Option.when(totalInterest > 0)(
          messages("overview.unpaidInterest.bullet")
        ),
        Option.when(countLPPNotPaidOrAppealed > 0)(
          messages(pluralOrSingular("overview.lpp.bullet.penalties", countLPPNotPaidOrAppealed), countLPPNotPaidOrAppealed)
        ),
        Option.when(countLSPNotPaidOrAppealed > 0)(
          messages(pluralOrSingular("overview.lsp.bullet.penalties", countLSPNotPaidOrAppealed), countLSPNotPaidOrAppealed)
        ),
        Option.when(lspPointsActive > 0)(
          if(lspPointsActive < lspThreshold) {
            messages(pluralOrSingular("overview.lsp.bullet.points", lspPointsActive), lspPointsActive)
          } else {
            messages("overview.lsp.bullet.points.max")
          }
        )
      ).flatten,
      hasFinancialCharge = hasFinancialChargeToPay
    )
  }
}
