/*
 * Copyright 2024 HM Revenue & Customs
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
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.{CurrentUserRequest, PenaltyDetails}
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.lpp.LPPDetails
import uk.gov.hmrc.incometaxpenaltiesfrontend.services.PenaltiesService
import uk.gov.hmrc.incometaxpenaltiesfrontend.utils.Logger.logger
import uk.gov.hmrc.incometaxpenaltiesfrontend.views.html.PenaltyCalculation
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PenaltyCalculationController @Inject()(override val controllerComponents: MessagesControllerComponents,
                                             penaltyCalculationView: PenaltyCalculation,
                                             authorised: AuthAction,
                                             errorHandler: ErrorHandler,
                                             penaltiesService: PenaltiesService,
                                             withNavBar: NavBarRetrievalAction)(implicit appConfig: AppConfig, ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val penaltyCalculationPage: Action[AnyContent] =
    (authorised andThen withNavBar).async { implicit currentUserRequest => withPenaltyData {penaltyData: PenaltyDetails =>

      val lpp: Option[LPPDetails] = for {
        latePaymentPenalty <- penaltyData.latePaymentPenalty
        lppDetail <- latePaymentPenalty.details.find(lppdetails =>
          lppdetails.LPP1LRCalculationAmount.isDefined &&
            lppdetails.LPP1HRCalculationAmount.isDefined &&
            lppdetails.LPP1LRPercentage.isDefined &&
            lppdetails.LPP1HRPercentage.isDefined
        )
      } yield lppDetail

      Future(Ok(penaltyCalculationView(isAgent = currentUserRequest.isAgent, lpp)))
    }
  }

  private[controllers] def withPenaltyData(block: PenaltyDetails => Future[Result])(implicit user: CurrentUserRequest[_]): Future[Result] =
    penaltiesService.getPenaltyDataForUser().flatMap(_.fold(
      error => {
        logger.error(s"[PenaltyCalculationController][PenaltyCalculationPage] Received error with message ${error.message} rendering ISE")
        errorHandler.showInternalServerError()
      },
      block
    ))
}


