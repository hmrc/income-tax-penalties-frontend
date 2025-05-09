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
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.incometaxpenaltiesfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesfrontend.controllers.predicates.{AuthAction, NavBarRetrievalAction, PenaltyDataAction}
//import uk.gov.hmrc.incometaxpenaltiesfrontend.models.AuthenticatedUserWithPenaltyData
//import uk.gov.hmrc.incometaxpenaltiesfrontend.models.lpp.LPPDetails
//import uk.gov.hmrc.incometaxpenaltiesfrontend.services.PenaltiesService
//import uk.gov.hmrc.incometaxpenaltiesfrontend.utils.Logger.logger
import uk.gov.hmrc.incometaxpenaltiesfrontend.views.html.PenaltyCalculation
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class PenaltyCalculationController @Inject()(override val controllerComponents: MessagesControllerComponents,
                                             penaltyCalculationView: PenaltyCalculation,
                                             authorised: AuthAction,
//                                             errorHandler: ErrorHandler,
//                                             penaltiesService: PenaltiesService,
                                             withNavBar: NavBarRetrievalAction,
                                             penaltyDataAction: PenaltyDataAction
                                            )(implicit appConfig: AppConfig, ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def penaltyCalculationPage(index: Int): Action[AnyContent] =
    (authorised andThen withNavBar) { implicit currentUserRequest =>
      Ok(penaltyCalculationView(currentUserRequest.isAgent))
    }
//  def penaltyCalculationPage(index: Int): Action[AnyContent] =
//    (authorised andThen withNavBar andThen penaltyDataAction).async { implicit penaltyDataRequest =>
//
//      getSelectedLPPDetails(index) match {
//        case Some(lppDetails) => Future(Ok(penaltyCalculationView(isAgent = penaltyDataRequest.isAgent, lppDetails)))
//        case None =>
//          logger.warn(s"[PenaltyCalculationController][penaltyCalculationPage] unable to find penalty details for index $index")
//          Future.successful(Redirect(routes.IndexController.homePage))
//      }
//
//
//    }
//
//  private def getSelectedLPPDetails(index: Int)
//                                   (implicit penaltyDataRequest: AuthenticatedUserWithPenaltyData[_]): Option[LPPDetails] = {
//    penaltyDataRequest
//      .penaltyDetails
//      .lpp
//      .zipWithIndex
//      .collectFirst{
//        case (lppDetails, lppIndex) if lppIndex == index => lppDetails
//      }
//  }
}


