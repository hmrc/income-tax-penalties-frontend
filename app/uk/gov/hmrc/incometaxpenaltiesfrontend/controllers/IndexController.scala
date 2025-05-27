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

package uk.gov.hmrc.incometaxpenaltiesfrontend.controllers

import play.api.i18n.I18nSupport
import play.api.mvc._
import uk.gov.hmrc.incometaxpenaltiesfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesfrontend.controllers.auth.actions.AuthActions
import uk.gov.hmrc.incometaxpenaltiesfrontend.controllers.auth.models.AuthenticatedUserWithPenaltyData
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.PenaltyDetails
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.lsp.{LSPDetails, LSPPenaltyStatusEnum}
import uk.gov.hmrc.incometaxpenaltiesfrontend.utils.IncomeTaxSessionKeys
import uk.gov.hmrc.incometaxpenaltiesfrontend.viewModels.{LSPOverviewViewModel, PenaltiesOverviewViewModel}
import uk.gov.hmrc.incometaxpenaltiesfrontend.views.helpers.{LPPCardHelper, LSPCardHelper}
import uk.gov.hmrc.incometaxpenaltiesfrontend.views.html.IndexView
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class IndexController @Inject()(override val controllerComponents: MessagesControllerComponents,
                                authActions: AuthActions,
                                lspCardHelper: LSPCardHelper,
                                lppCardHelper: LPPCardHelper,
                                indexView: IndexView)(implicit appConfig: AppConfig, ec: ExecutionContext) extends FrontendBaseController with I18nSupport {


  def homePage(isAgent:Boolean): Action[AnyContent] = authActions.asMTDUserWithPenaltyData(isAgent).async { implicit penaltyDataUserRequest =>
    val penaltyData = penaltyDataUserRequest.penaltyDetails
    val lsp = penaltyData.lateSubmissionPenalty.map(_.details).getOrElse(Seq.empty)
    val lspThreshold = penaltyData.lateSubmissionPenalty.map(_.summary.regimeThreshold).getOrElse(0)
    val lspActivePoints = penaltyData.lateSubmissionPenalty.map(_.summary.activePenaltyPoints).getOrElse(0)

    val lspSummaryCards = lspCardHelper.createLateSubmissionPenaltyCards(
      penalties = sortPointsInDescendingOrder(lsp),
      threshold = lspThreshold,
      activePoints = lspActivePoints
    )

    val lpp = penaltyData.latePaymentPenalty.map(_.details).map(_.sorted).getOrElse(Seq.empty)
    val lppSummaryCards = lppCardHelper.createLatePaymentPenaltyCards(lpp.zipWithIndex)

    Future(
      updateSessionCookie(penaltyData) {
        Ok(indexView(
          lspOverviewData = penaltyData.lateSubmissionPenalty.map(LSPOverviewViewModel.apply),
          lspCardData = lspSummaryCards,
          lppCardData = lppSummaryCards,
          penaltiesOverviewViewModel = PenaltiesOverviewViewModel(penaltyData),
          isAgent = penaltyDataUserRequest.isAgent
        ))
      }
    )
  }

  private[controllers] def updateSessionCookie(penaltyData: PenaltyDetails)(result: => Result)(implicit req: Request[_]): Result = {

    val optPOCAchievementDate: Option[String] = penaltyData.lateSubmissionPenalty.flatMap(_.summary.PoCAchievementDate.map(_.toString))
    val optRegimeThreshold = penaltyData.lateSubmissionPenalty.map(_.summary.regimeThreshold.toString)

    result
      .removingFromSession(
        IncomeTaxSessionKeys.pocAchievementDate,
        IncomeTaxSessionKeys.regimeThreshold
      )
      .addingToSession(
        Seq(
          optPOCAchievementDate.map(IncomeTaxSessionKeys.pocAchievementDate -> _),
          optRegimeThreshold.map(IncomeTaxSessionKeys.regimeThreshold -> _)
        ).flatten:_*
      )
  }

  def sortPointsInDescendingOrder(points: Seq[LSPDetails]): Seq[LSPDetails] = {
    val pointsWithOrder = points.zipWithIndex.map {
      case (point, idx) =>
        val newPenaltyOrder = (point.penaltyOrder, point.penaltyStatus) match {
          case (None, LSPPenaltyStatusEnum.Inactive) => Some("0")
          case (None, LSPPenaltyStatusEnum.Active) => Some((idx + 1).toString)
          case _ => point.penaltyOrder
        }
        point.copy(penaltyOrder = newPenaltyOrder)
    }

    pointsWithOrder.sortWith((thisElement, nextElement) => thisElement.penaltyOrder.getOrElse("0").toInt > nextElement.penaltyOrder.getOrElse("0").toInt)
  }
}


