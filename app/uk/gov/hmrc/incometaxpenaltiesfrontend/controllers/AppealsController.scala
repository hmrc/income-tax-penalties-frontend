/*
 * Copyright 2023 HM Revenue & Customs
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

import play.api.mvc._
import uk.gov.hmrc.incometaxpenaltiesfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesfrontend.controllers.auth.actions.AuthActions
import uk.gov.hmrc.incometaxpenaltiesfrontend.featureswitch.core.config.FeatureSwitching
import uk.gov.hmrc.incometaxpenaltiesfrontend.utils.DateFormatter
import uk.gov.hmrc.incometaxpenaltiesfrontend.utils.Logger.logger
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject

class AppealsController @Inject()(val authActions: AuthActions,
                                  override val controllerComponents: MessagesControllerComponents
                                 )(implicit val appConfig: AppConfig) extends FrontendBaseController with FeatureSwitching with DateFormatter {

  def redirectToAppeals(penaltyId: String,
                        isAgent: Boolean,
                        isLPP: Boolean,
                        isFindOutHowToAppealLSP: Boolean,
                        isLPP2: Boolean,
                        is2ndStageAppeal: Boolean): Action[AnyContent] =
    authActions.asMTDUser(isAgent) { _ =>
      logger.debug(s"[IndexController][redirectToAppeals] - Redirect to appeals frontend with id $penaltyId and is late payment penalty: $isLPP " +
        s"and cannot be appealed: $isFindOutHowToAppealLSP and is LPP2: $isLPP2")
      if (isFindOutHowToAppealLSP) {
        Redirect(s"${appConfig.incomeTaxPenaltiesAppealsBaseUrl}" + s"/appeal-start")
      } else {
        Redirect(s"${appConfig.incomeTaxPenaltiesAppealsBaseUrl}/initialise-appeal?penaltyId=$penaltyId&isAgent=$isAgent&isLPP=$isLPP&isAdditional=$isLPP2&is2ndStageAppeal=$is2ndStageAppeal")
      }
    }


  def redirectToFindOutHowToAppealLPP(principalChargeReference: String,
                                      isAgent: Boolean,
                                      itsaAmountInPence: Int,
                                      itsaPeriodStartDate: String,
                                      itsaPeriodEndDate: String): Action[AnyContent] =
    authActions.asMTDUser(isAgent) { _ =>
      logger.debug(s"[IndexController][redirectToFindOutHowToAppealLPP] - Redirect to appeals frontend with principleChargeReference: $principalChargeReference " +
        s"and has itsaPeriodStartDate: $itsaPeriodStartDate and has itsaPeriodEndDate: $itsaPeriodEndDate and has itsaAmountInPence: $itsaAmountInPence")
      Redirect(s"${appConfig.incomeTaxPenaltiesAppealsBaseUrl}/initialise-appeal-find-out-how-to-appeal?principalChargeReference=$principalChargeReference&isAgent=$isAgent&itsaAmountInPence=$itsaAmountInPence&itsaPeriodStartDate=$itsaPeriodStartDate&itsaPeriodEndDate=$itsaPeriodEndDate")
    }

}
