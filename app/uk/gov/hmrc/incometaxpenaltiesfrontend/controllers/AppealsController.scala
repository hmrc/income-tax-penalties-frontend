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
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.incometaxpenaltiesfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesfrontend.utils.ImplicitDateFormatter
import uk.gov.hmrc.incometaxpenaltiesfrontend.controllers.auth.AuthenticatedController
import uk.gov.hmrc.incometaxpenaltiesfrontend.featureswitch.core.config.FeatureSwitching
import uk.gov.hmrc.incometaxpenaltiesfrontend.utils.Logger.logger

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AppealsController @Inject()(val authConnector: AuthConnector,
                                  mcc: MessagesControllerComponents
                                 )(implicit val appConfig: AppConfig,
                                   ec: ExecutionContext) extends AuthenticatedController(mcc) with FeatureSwitching with ImplicitDateFormatter {

  def redirectToAppeals(penaltyId: String,
                        isLPP: Boolean,
                        isFindOutHowToAppealLSP: Boolean,
                        isLPP2: Boolean): Action[AnyContent] =
    isAuthenticated {
      implicit request =>
        implicit currentUser =>
      logger.debug(s"[IndexController][redirectToAppeals] - Redirect to appeals frontend with id $penaltyId and is late payment penalty: $isLPP " +
        s"and cannot be appealed: $isFindOutHowToAppealLSP and is LPP2: $isLPP2")
      if (isFindOutHowToAppealLSP) {
        Future(Redirect(s"${appConfig.incomeTaxPenaltiesAppealsBaseUrl}" +
          s"/initialise-appeal-against-the-obligation?penaltyId=$penaltyId"))
      } else {
        Future(Redirect(s"${appConfig.incomeTaxPenaltiesAppealsBaseUrl}/initialise-appeal?penaltyId=$penaltyId&isLPP=$isLPP&isAdditional=$isLPP2"))
      }
    }


  def redirectToFindOutHowToAppealLPP(principalChargeReference: String,
                                      itsaAmountInPence: Int,
                                      itsaPeriodStartDate: String,
                                      itsaPeriodEndDate: String): Action[AnyContent] =
    isAuthenticated {
      implicit request =>
        implicit currentUser =>
      logger.debug(s"[IndexController][redirectToFindOutHowToAppealLPP] - Redirect to appeals frontend with principleChargeReference: $principalChargeReference " +
        s"and has itsaPeriodStartDate: $itsaPeriodStartDate and has itsaPeriodEndDate: $itsaPeriodEndDate and has itsaAmountInPence: $itsaAmountInPence")
      Future(Redirect(s"${appConfig.incomeTaxPenaltiesAppealsBaseUrl}/initialise-appeal-find-out-how-to-appeal?principalChargeReference=$principalChargeReference&itsaAmountInPence=$itsaAmountInPence&itsaPeriodStartDate=$itsaPeriodStartDate&itsaPeriodEndDate=$itsaPeriodEndDate"))
    }

}
