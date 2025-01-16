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

package uk.gov.hmrc.incometaxpenaltiesfrontend.views.helpers

import play.api.i18n.Messages
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.appealInfo.AppealStatusEnum
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.lsp._
import uk.gov.hmrc.incometaxpenaltiesfrontend.utils._
import uk.gov.hmrc.incometaxpenaltiesfrontend.viewModels.LateSubmissionPenaltySummaryCard

import javax.inject.Inject

class LSPCardHelper @Inject()(summaryRow: LSPSummaryListRowHelper) extends SummaryListRowHelper with TagHelper with DateFormatter {

  def createLateSubmissionPenaltyCards(penalties: Seq[LSPDetails],
                                       threshold: Int,
                                       activePoints: Int)
                                      (implicit messages: Messages): Seq[LateSubmissionPenaltySummaryCard] = {

    val activePenalties: Seq[(LSPDetails, Int)] =
      penalties.filter(_.penaltyStatus != LSPPenaltyStatusEnum.Inactive).reverse.zipWithIndex

    penalties.map { penalty =>
      val penaltyWithPoints = findAndReindexPointIfIsActive(activePenalties, penalty)
      penaltyWithPoints.lspTypeEnum match {
        case LSPTypeEnum.AddedFAP =>
          addedPointCard(penaltyWithPoints, activePoints >= threshold)
        case LSPTypeEnum.RemovedFAP | LSPTypeEnum.RemovedPoint =>
          removedPointCard(penaltyWithPoints)
        case LSPTypeEnum.AppealedPoint | LSPTypeEnum.Point =>
          pointSummaryCard(penaltyWithPoints, activePoints >= threshold)
        case _ =>
          financialSummaryCard(penaltyWithPoints)
      }
    }
  }

  def addedPointCard(penalty: LSPDetails, thresholdMet: Boolean)(implicit messages: Messages): LateSubmissionPenaltySummaryCard = {
    buildLSPSummaryCard(
      cardTitle = messages("lsp.cardTitle.addedPoint", penalty.penaltyOrder.getOrElse("")),
      rows = Seq(
        Some(summaryListRow(
          label = messages("lsp.addedOn.key"),
          value = Html(dateToString(penalty.penaltyCreationDate))
        )),
        Option.when(!thresholdMet)(summaryRow.pointExpiryDate(penalty))
      ).flatten,
      penalty = penalty,
      isAnAddedPoint = true,
      isAnAddedOrRemovedPoint = true
    )
  }

  def financialSummaryCard(penalty: LSPDetails)(implicit messages: Messages): LateSubmissionPenaltySummaryCard =
    buildLSPSummaryCard(
      cardTitle = messages(
        "lsp.cardTitle.financialPoint",
        penalty.penaltyOrder.getOrElse(""),
        CurrencyFormatter.parseBigDecimalNoPaddedZeroToFriendlyValue(penalty.originalAmount)
      ),
      rows = Seq(
        summaryRow.taxPeriodSummaryRow(penalty),
        summaryRow.dueDateSummaryRow(penalty),
        Some(summaryRow.receivedDateSummaryRow(penalty)),
        summaryRow.appealStatusRow(penalty.appealStatus, penalty.appealLevel)
      ).flatten,
      penalty = penalty
    )


  def pointSummaryCard(penalty: LSPDetails, thresholdMet: Boolean)(implicit messages: Messages): LateSubmissionPenaltySummaryCard = {
    buildLSPSummaryCard(
      cardTitle = messages("lsp.cardTitle.point", penalty.penaltyOrder.getOrElse("")),
      rows = Seq(
        summaryRow.taxPeriodSummaryRow(penalty),
        summaryRow.dueDateSummaryRow(penalty),
        Some(summaryRow.receivedDateSummaryRow(penalty)),
        Option.when(!thresholdMet && !penalty.appealStatus.contains(AppealStatusEnum.Upheld)) {
          summaryRow.pointExpiryDate(penalty)
        }
      ).flatten,
      penalty = penalty
    )
  }

  def removedPointCard(penalty: LSPDetails)(implicit messages: Messages): LateSubmissionPenaltySummaryCard = {
    buildLSPSummaryCard(
      cardTitle = messages("lsp.cardTitle.removedPoint"),
      rows = Seq(
        summaryRow.taxPeriodSummaryRow(penalty),
        summaryRow.expiryReasonSummaryRow(penalty),
      ).flatten,
      penalty = penalty,
      isAnAddedOrRemovedPoint = true,
      isManuallyRemovedPoint = !penalty.isFAP
    )
  }

  private def buildLSPSummaryCard(cardTitle: String,
                                  rows: Seq[SummaryListRow],
                                  penalty: LSPDetails,
                                  isAnAddedPoint: Boolean = false,
                                  isAnAddedOrRemovedPoint: Boolean = false,
                                  isManuallyRemovedPoint: Boolean = false)(implicit messages: Messages): LateSubmissionPenaltySummaryCard = {
    LateSubmissionPenaltySummaryCard(
      cardRows = rows,
      cardTitle = cardTitle,
      status = getTagStatus(penalty),
      penaltyPoint = penalty.penaltyOrder.getOrElse(""),
      penaltyId = penalty.penaltyNumber,
      isReturnSubmitted = penalty.isReturnSubmitted,
      isAddedPoint = isAnAddedPoint,
      isAppealedPoint = penalty.appealStatus.getOrElse(AppealStatusEnum.Unappealable) != AppealStatusEnum.Unappealable,
      appealStatus = penalty.appealStatus,
      appealLevel = penalty.appealLevel,
      isAddedOrRemovedPoint = isAnAddedOrRemovedPoint,
      isManuallyRemovedPoint = isManuallyRemovedPoint,
      dueDate = penalty.dueDate.map(dateToString(_)),
      penaltyCategory = penalty.penaltyCategory
    )
  }

  private def findAndReindexPointIfIsActive(indexedActivePoints: Seq[(LSPDetails, Int)], penaltyPoint: LSPDetails): LSPDetails = {
    if (indexedActivePoints.map(_._1).contains(penaltyPoint)) {
      val numberOfPoint = indexedActivePoints.find(_._1 == penaltyPoint).get._2 + 1
      penaltyPoint.copy(penaltyOrder = Some(s"$numberOfPoint"))
    } else {
      penaltyPoint
    }
  }
}
