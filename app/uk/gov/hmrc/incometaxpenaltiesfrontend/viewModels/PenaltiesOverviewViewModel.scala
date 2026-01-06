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
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.penaltyDetails.PenaltyDetails
import uk.gov.hmrc.incometaxpenaltiesfrontend.views.ViewUtils.pluralOrSingular


sealed trait PenaltiesOverviewItem {
  val name: String

  def messageKey(hasBullets: Boolean, isAgent: Boolean): String = {

    val key = s"index.overview.$name"

    if (hasBullets) s"$key.bullet"
    else if (isAgent) s"agent.$key"

    else s"individual.$key"
  }

  def content(hasMultipleBullets: Boolean, isAgent: Boolean)(implicit messages: Messages): String
}

final case class SimpleOverviewItem(override val name: String) extends PenaltiesOverviewItem {
  override def content(hasMultipleBullets: Boolean, isAgent: Boolean)(implicit messages: Messages): String =
    messages(messageKey(hasMultipleBullets, isAgent))
}

val UnpaidReturnChargesOverviewItem: PenaltiesOverviewItem = SimpleOverviewItem("unpaidReturnCharges")
val UnpaidInterestItem: PenaltiesOverviewItem = SimpleOverviewItem("unpaidInterest")


abstract class CountOverviewItem(override val name: String, count: Int) extends PenaltiesOverviewItem {
  override def content(hasMultipleBullets: Boolean, isAgent: Boolean)(implicit messages: Messages): String =
    messages(pluralOrSingular(messageKey(hasMultipleBullets, isAgent), count), count)
}

case class LPPNotPaidOrAppealed(count: Int) extends CountOverviewItem("lpp.penalties", count)

case class LSPNotPaidOrAppealed(count: Int) extends CountOverviewItem("lsp.penalties", count)

case class LSPPointsActive(count: Int) extends CountOverviewItem("lsp.points", count)

object LSPMaxItem extends PenaltiesOverviewItem {
  override val name: String = "lsp.points.max"

  override def content(hasMultipleBullets: Boolean, isAgent: Boolean)(implicit messages: Messages): String = {
    messages(messageKey(hasMultipleBullets, isAgent))
  }
}

case class PenaltiesOverviewViewModel(overviewItems: Seq[PenaltiesOverviewItem], hasFinancialCharge: Boolean) {

  def content(isAgent: Boolean)(implicit messages: Messages): Seq[String] = {
    if (overviewItems.size > 1) {
      overviewItems.map(_.content(hasMultipleBullets = true, isAgent))
    } else {
      overviewItems.map(_.content(hasMultipleBullets = false, isAgent))
    }
  }
}

object PenaltiesOverviewViewModel {

  def apply(penaltyDetails: PenaltyDetails)(implicit messages: Messages): PenaltiesOverviewViewModel = {
    import penaltyDetails.*

    val whatOverviewDetails = Seq(

      Option.when(unpaidIncomeTax > 0)(
        UnpaidReturnChargesOverviewItem
      ),
      Option.when(totalInterest > 0)(
        UnpaidInterestItem
      ),
      Option.when(countLPPNotPaidOrAppealed > 0)(
        LPPNotPaidOrAppealed(countLPPNotPaidOrAppealed)
      ),
      Option.when(countLSPNotPaidOrAppealed > 0)(
        LSPNotPaidOrAppealed(countLSPNotPaidOrAppealed)
      ),
      Option.when(lspPointsActive > 0)(
        if (lspPointsActive < lspThreshold) {
          LSPPointsActive(lspPointsActive)
        } else {
          LSPMaxItem
        }
      )
    ).flatten
    PenaltiesOverviewViewModel(
      overviewItems = whatOverviewDetails,
      hasFinancialCharge = hasFinancialChargeToPay
    )
  }
}
