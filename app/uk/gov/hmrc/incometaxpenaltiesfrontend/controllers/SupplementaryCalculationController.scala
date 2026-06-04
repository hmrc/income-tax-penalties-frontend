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
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.penaltyDetails.lpp.{LPPDetails, LPPPenaltyCategoryEnum}
import uk.gov.hmrc.incometaxpenaltiesfrontend.services.AuditService
import uk.gov.hmrc.incometaxpenaltiesfrontend.utils.Logger.logger
import uk.gov.hmrc.incometaxpenaltiesfrontend.utils.TimeMachine
import uk.gov.hmrc.incometaxpenaltiesfrontend.viewModels.{FirstLatePaymentPenaltyCalculationData, SecondLatePaymentPenaltyCalculationData}
import uk.gov.hmrc.incometaxpenaltiesfrontend.views.html.Lpp1Supplement
import uk.gov.hmrc.incometaxpenaltiesfrontend.views.html.Lpp2Supplement
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject

@Singleton
class SupplementaryCalculationController @Inject()(override val controllerComponents: MessagesControllerComponents,
                                                    lpp1SupplementView: Lpp1Supplement,
                                                    lpp2Supplement: Lpp2Supplement,
                                                   authActions: AuthActions,
                                                   auditService: AuditService)
                                                   (implicit appConfig: AppConfig, timeMachine: TimeMachine) extends FrontendBaseController with I18nSupport {

  private def matchesLPP2Supplement(lpp: LPPDetails, penaltyId: String): Boolean =
    (lpp.penaltyChargeReference.contains(penaltyId) || lpp.principalChargeReference == penaltyId) &&
      lpp.penaltyCategory == LPPPenaltyCategoryEnum.LPP2 &&
      lpp.supplement.contains(true)

  def supplementaryCalculationPage(penaltyId: String,
                                    isAgent: Boolean): Action[AnyContent] =
    authActions.asMTDUserWithPenaltyData(isAgent) {
      implicit currentUserRequest =>
        val allLpps = currentUserRequest.penaltyDetails.latePaymentPenalty.map(_.details).getOrElse(Seq.empty)
        val penaltyDetailsForId = allLpps.find(lpp =>
          lpp.principalChargeReference == penaltyId &&
            lpp.penaltyCategory == LPPPenaltyCategoryEnum.LPP1 &&
            lpp.supplement.contains(true)
        )

        penaltyDetailsForId match {
          case Some(lppDetails) =>
            logger.info(s"[SupplementaryCalculationController][supplementaryCalculationPage] Found LPP1 supplement for penaltyId=$penaltyId")
            val auditEvent = new UserCalculationInfoAuditModel(lppDetails)
            auditService.audit(auditEvent)(implicitly)
            Ok(lpp1SupplementView(new FirstLatePaymentPenaltyCalculationData(lppDetails), isAgent, timeMachine))
          case _ =>
            logger.warn(
              s"[SupplementaryCalculationController][supplementaryCalculationPage] No LPP1 supplement found for penaltyId=$penaltyId. " +
              s"LPP count=${allLpps.size}, " +
              s"LPP1 supplements present=[${allLpps.filter(l => l.penaltyCategory == LPPPenaltyCategoryEnum.LPP1 && l.supplement.contains(true)).map(_.principalChargeReference).mkString(",")}]"
            )
            Redirect(routes.IndexController.homePage(isAgent))
        }
    }

  def supplementaryCalculationPageLPP2(penaltyId: String,
                                   isAgent: Boolean): Action[AnyContent] =
    authActions.asMTDUserWithPenaltyData(isAgent) {
      implicit currentUserRequest =>
        val allLpps = currentUserRequest.penaltyDetails.latePaymentPenalty.map(_.details).getOrElse(Seq.empty)
        val penaltyDetailsForId = allLpps.find(matchesLPP2Supplement(_, penaltyId))

        penaltyDetailsForId match {
          case Some(lppDetails) =>
            logger.info(s"[SupplementaryCalculationController][supplementaryCalculationPageLPP2] Found LPP2 supplement for penaltyId=$penaltyId penaltyChargeReference=${lppDetails.penaltyChargeReference}")
            val auditEvent = new UserCalculationInfoAuditModel(lppDetails)
            auditService.audit(auditEvent)(implicitly)
            Ok(lpp2Supplement(new SecondLatePaymentPenaltyCalculationData(lppDetails), isAgent, timeMachine))
          case _ =>
            logger.warn(
              s"[SupplementaryCalculationController][supplementaryCalculationPageLPP2] No LPP2 supplement found for penaltyId=$penaltyId. " +
              s"LPP count=${allLpps.size}, " +
              s"LPP2 supplements present=[${allLpps.filter(l => l.penaltyCategory == LPPPenaltyCategoryEnum.LPP2 && l.supplement.contains(true)).map(l => s"pcr=${l.principalChargeReference} penRef=${l.penaltyChargeReference} supplement=${l.supplement}").mkString("; ")}]"
            )
            Redirect(routes.IndexController.homePage(isAgent))
        }
    }
}
