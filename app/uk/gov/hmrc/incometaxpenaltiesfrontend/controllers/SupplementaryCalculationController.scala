/*
 * Copyright 2026 HM Revenue & Customs
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

import com.google.inject.Singleton
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.incometaxpenaltiesfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesfrontend.controllers.auth.actions.AuthActions
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.audit.UserCalculationInfoAuditModel
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.penaltyDetails.lpp.LPPDetails
import uk.gov.hmrc.incometaxpenaltiesfrontend.services.AuditService
import uk.gov.hmrc.incometaxpenaltiesfrontend.utils.TimeMachine
import uk.gov.hmrc.incometaxpenaltiesfrontend.viewModels.FirstLatePaymentPenaltyCalculationData
import uk.gov.hmrc.incometaxpenaltiesfrontend.views.html.Lpp1Supplement
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject

@Singleton
class SupplementaryCalculationController @Inject()(override val controllerComponents: MessagesControllerComponents,
                                                   lpp1SupplementView: Lpp1Supplement,
                                                   authActions: AuthActions,
                                                   auditService: AuditService)
                                                  (implicit appConfig: AppConfig, timeMachine: TimeMachine) extends FrontendBaseController with I18nSupport {


  def supplementaryCalculationPage(penaltyId: String,
                                   isAgent: Boolean): Action[AnyContent] =
    authActions.asMTDUserWithPenaltyData(isAgent) {
      implicit currentUserRequest =>
        val penaltyDetailsForId = currentUserRequest
          .penaltyDetails
          .latePaymentPenalty
          .flatMap {
            _.details.collectFirst { case lpp if lpp.principalChargeReference == penaltyId => lpp }
          }
        
        
        val date = timeMachine.getCurrentDate()
        val isInBreathingSpace = currentUserRequest.penaltyDetails.breathingSpace.fold(false)(_.count(bs =>
          (bs.bsStartDate.isEqual(date) || bs.bsStartDate.isBefore(date)) &&
            (bs.bsEndDate.isEqual(date) || bs.bsEndDate.isAfter(date))
        ) > 0)

        penaltyDetailsForId match {
          case Some(lppDetails) if lppDetails.supplement.contains(true) =>
            val auditEvent = new UserCalculationInfoAuditModel(lppDetails)
            auditService.audit(auditEvent)(implicitly)
            Ok(lpp1SupplementView(new FirstLatePaymentPenaltyCalculationData(lppDetails), isAgent, timeMachine, isInBreathingSpace, currentUserRequest.penaltyDetails.breathingSpace))
          case _ =>
            Redirect(routes.IndexController.homePage(isAgent))
        }
    }
}