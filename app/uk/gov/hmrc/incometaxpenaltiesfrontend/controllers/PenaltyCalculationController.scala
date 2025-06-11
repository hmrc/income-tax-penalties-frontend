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
import uk.gov.hmrc.incometaxpenaltiesfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesfrontend.controllers.auth.actions.AuthActions
import uk.gov.hmrc.incometaxpenaltiesfrontend.controllers.auth.models.AuthenticatedUserWithPenaltyData
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.lpp.LPPDetails
import uk.gov.hmrc.incometaxpenaltiesfrontend.utils.TimeMachine
import uk.gov.hmrc.incometaxpenaltiesfrontend.viewModels._
import uk.gov.hmrc.incometaxpenaltiesfrontend.views.html._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.{Inject, Singleton}

@Singleton
class PenaltyCalculationController @Inject()(override val controllerComponents: MessagesControllerComponents,
                                             firstPenaltyCalculationView: FirstPenaltyCalculation,
                                             secondPenaltyCalculationView: SecondPenaltyCalculation,
                                             authActions: AuthActions)
                                            (implicit appConfig: AppConfig, timeMachine: TimeMachine) extends FrontendBaseController with I18nSupport {

  def penaltyCalculationPage(penaltyId: String, isAgent: Boolean, isLPP2: Boolean): Action[AnyContent] = authActions.asMTDUserWithPenaltyData(isAgent)  { implicit currentUserRequest =>
    val penaltyDetailsForId = currentUserRequest
      .penaltyDetails
      .latePaymentPenalty
      .fold[Option[LPPDetails]](None)(_.details.collectFirst{
        case lpp if lpp.principalChargeReference ==  penaltyId => lpp
      })

    penaltyDetailsForId match {
      case Some(lppDetails) if isLPP2 => handleLPP2(lppDetails, isAgent)
      case Some(lppDetails) => handleLPP1(lppDetails, isAgent)
      case None => Redirect(routes.IndexController.homePage(isAgent))
    }
  }

  def handleLPP1[A](lppDetails: LPPDetails,
                    isAgent: Boolean)
                   (implicit currentUserRequest: AuthenticatedUserWithPenaltyData[A]): Result = {
    val calculationData = new FirstLatePaymentPenaltyCalculationData(lppDetails)
    Ok(firstPenaltyCalculationView(calculationData, currentUserRequest.isAgent))

  }

  def handleLPP2[A](lppDetails: LPPDetails, isAgent: Boolean)(implicit currentUserRequest: AuthenticatedUserWithPenaltyData[A]): Result = {
    val calculationData = new SecondLatePaymentPenaltyCalculationData(lppDetails)
    Ok(secondPenaltyCalculationView(calculationData, currentUserRequest.isAgent))

  }
}


