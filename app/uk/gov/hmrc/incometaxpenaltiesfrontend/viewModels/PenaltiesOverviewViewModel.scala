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

object UnpaidReturnCharges0verviewItem extends PenaltiesOverviewItem {
  override val name: String = "unpaidReturnCharges"

  override def content(hasMultipleBullets: Boolean, isAgent: Boolean)(implicit messages: Messages): String = {
    messages(messageKey(hasMultipleBullets, isAgent))
  }
}

object UnpaidInterestItem extends PenaltiesOverviewItem {
  override val name: String = "unpaidInterest"

  override def content(hasMultipleBullets: Boolean, isAgent: Boolean)(implicit messages: Messages): String = {
    messages(messageKey(hasMultipleBullets, isAgent))
  }
}

case class LPPNotPaidOrAppealed(count: Int) extends PenaltiesOverviewItem {
  override val name: String = "lpp.penalties"

  override def content(hasMultipleBullets: Boolean, isAgent: Boolean)(implicit messages: Messages): String = {
    messages(pluralOrSingular(messageKey(hasMultipleBullets, isAgent), count), count)
  }
}

case class LSPNotPaidOrAppealed(count: Int) extends PenaltiesOverviewItem {
  override val name: String = "lsp.penalties"

  override def content(hasMultipleBullets: Boolean, isAgent: Boolean)(implicit messages: Messages): String = {
    messages(pluralOrSingular(messageKey(hasMultipleBullets, isAgent), count), count)
  }
}

case class LSPPointsActive(count: Int) extends PenaltiesOverviewItem {
  override val name: String = "lsp.points"

  override def content(hasMultipleBullets: Boolean, isAgent: Boolean)(implicit messages: Messages): String = {
    messages(pluralOrSingular(messageKey(hasMultipleBullets, isAgent), count), count)
  }
}

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
    import penaltyDetails._

    val whatOverviewDetails = Seq(

      Option.when(unpaidIncomeTax > 0)(
        UnpaidReturnCharges0verviewItem
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
