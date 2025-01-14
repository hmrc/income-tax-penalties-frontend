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
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.incometaxpenaltiesfrontend.config.{AppConfig, ErrorHandler}
import uk.gov.hmrc.incometaxpenaltiesfrontend.controllers.predicates.{AuthAction, NavBarRetrievalAction}
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.lsp.{LSPDetails, LSPPenaltyStatusEnum}
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.{CurrentUserRequest, PenaltyDetails}
import uk.gov.hmrc.incometaxpenaltiesfrontend.services.PenaltiesService
import uk.gov.hmrc.incometaxpenaltiesfrontend.utils.IncomeTaxSessionKeys
import uk.gov.hmrc.incometaxpenaltiesfrontend.utils.Logger.logger
import uk.gov.hmrc.incometaxpenaltiesfrontend.views.helpers.LSPCardHelper
import uk.gov.hmrc.incometaxpenaltiesfrontend.views.html.IndexView
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class IndexController @Inject()(override val controllerComponents: MessagesControllerComponents,
                                authorised: AuthAction,
                                withNavBar: NavBarRetrievalAction,
                                penaltiesService: PenaltiesService,
                                errorHandler: ErrorHandler,
                                lspCardHelper: LSPCardHelper,
                                indexView: IndexView)(implicit appConfig: AppConfig, ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val homePage: Action[AnyContent] = (authorised andThen withNavBar).async { implicit currentUserRequest =>
    withPenaltyData { penaltyData =>

      val lsp = penaltyData.lateSubmissionPenalty.map(_.details).getOrElse(Seq.empty)

      val lspSummaryCards = lspCardHelper.createLateSubmissionPenaltyCards(
        penalties = sortPointsInDescendingOrder(lsp),
        threshold = penaltyData.lateSubmissionPenalty.map(_.summary.regimeThreshold).getOrElse(0),
        activePoints = penaltyData.lateSubmissionPenalty.map(_.summary.activePenaltyPoints).getOrElse(0)
      )

      Future(
        updateSessionCookie(penaltyData) {
          Ok(indexView(lspSummaryCards, currentUserRequest.isAgent))
        }
      )
    }
  }

  private[controllers] def updateSessionCookie(penaltyData: PenaltyDetails)(result: => Result)(implicit user: CurrentUserRequest[_]): Result = {

    val optPOCAchievementDate: Option[String] = penaltyData.lateSubmissionPenalty.flatMap(_.summary.PoCAchievementDate.map(_.toString))
    val optRegimeThreshold = penaltyData.lateSubmissionPenalty.map(_.summary.regimeThreshold.toString)

    result
      .removingFromSession(
        IncomeTaxSessionKeys.pocAchievementDate,
        IncomeTaxSessionKeys.regimeThreshold
      )
      .addingToSession(
        Seq(
          Option.when(optPOCAchievementDate.isDefined)(IncomeTaxSessionKeys.pocAchievementDate -> optPOCAchievementDate.get),
          Option.when(optRegimeThreshold.isDefined)(IncomeTaxSessionKeys.regimeThreshold -> optRegimeThreshold.get)
        ).flatten:_*
      )
  }

  private[controllers] def withPenaltyData(block: PenaltyDetails => Future[Result])(implicit user: CurrentUserRequest[_]): Future[Result] =
    penaltiesService.getPenaltyDataForUser().flatMap(_.fold(
      error => {
        logger.error(s"[IndexController][homePage] Received error with message ${error.message} rendering ISE")
        errorHandler.showInternalServerError()
      },
      block
    ))

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


