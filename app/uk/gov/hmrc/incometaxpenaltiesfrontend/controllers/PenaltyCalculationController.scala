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
import uk.gov.hmrc.incometaxpenaltiesfrontend.controllers.auth.actions.AuthActions
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.audit.UserCalculationInfoAuditModel
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.penaltyDetails.lpp.{LPPDetails, LPPPenaltyCategoryEnum}
import uk.gov.hmrc.incometaxpenaltiesfrontend.services.AuditService
import uk.gov.hmrc.incometaxpenaltiesfrontend.utils.TimeMachine
import uk.gov.hmrc.incometaxpenaltiesfrontend.viewModels.*
import uk.gov.hmrc.incometaxpenaltiesfrontend.views.html.*
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.{Inject, Singleton}

@Singleton
class PenaltyCalculationController @Inject()(override val controllerComponents: MessagesControllerComponents,
                                             lpp1CalculationView: Lpp1Calculation,
                                             lpp2CalculationView: Lpp2Calculation,
                                             authActions: AuthActions,
                                             auditService: AuditService)
                                            (implicit appConfig: AppConfig, timeMachine: TimeMachine) extends FrontendBaseController with I18nSupport {

  def isCorrectLPP(lpp: LPPDetails, isLPP2: Boolean): Boolean = {
    if (isLPP2) {
      lpp.penaltyCategory == LPPPenaltyCategoryEnum.LPP2
    } else {
      lpp.penaltyCategory == LPPPenaltyCategoryEnum.LPP1
    }
  }

  def penaltyCalculationPage(penaltyId: String,
                             isAgent: Boolean,
                             isLPP2: Boolean): Action[AnyContent] =
    authActions.asMTDUserWithPenaltyData(isAgent) {
      implicit currentUserRequest =>
        val penaltyDetailsForId = currentUserRequest
          .penaltyDetails
          .latePaymentPenalty
          .flatMap {
            _.details.collectFirst { case lpp if lpp.principalChargeReference == penaltyId && isCorrectLPP(lpp, isLPP2) => lpp }
          }

        penaltyDetailsForId match {
          case Some(lppDetails) => {
            val auditEvent = new UserCalculationInfoAuditModel(lppDetails)
            auditService.audit(auditEvent)(implicitly)
            if (isLPP2) {
              Ok(lpp2CalculationView(new SecondLatePaymentPenaltyCalculationData(lppDetails), isAgent, timeMachine, currentUserRequest.penaltyDetails.isInBreathingSpace))
            } else {
              Ok(lpp1CalculationView(new FirstLatePaymentPenaltyCalculationData(lppDetails), isAgent, currentUserRequest.penaltyDetails.isInBreathingSpace))
            }
          }
          case None =>
            Redirect(routes.IndexController.homePage(isAgent))
        }
    }
}


