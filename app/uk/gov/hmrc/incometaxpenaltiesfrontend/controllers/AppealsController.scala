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
import uk.gov.hmrc.incometaxpenaltiesfrontend.controllers.predicates.AuthPredicate
import uk.gov.hmrc.incometaxpenaltiesfrontend.featureswitch.core.config.FeatureSwitching
import uk.gov.hmrc.incometaxpenaltiesfrontend.utils.ImplicitDateFormatter
import uk.gov.hmrc.incometaxpenaltiesfrontend.utils.Logger.logger
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AppealsController @Inject()(authorise: AuthPredicate,
                                  mcc: MessagesControllerComponents
                               )(implicit val appConfig: AppConfig, ec: ExecutionContext) extends FrontendController(mcc) with FeatureSwitching with ImplicitDateFormatter {

  def redirectToAppeals(penaltyId: String,
                        isLPP: Boolean = false,
                        isFindOutHowToAppealLSP: Boolean = false,
                        isLPP2: Boolean = false): Action[AnyContent] =
    authorise.async {
      logger.debug(s"[IndexController][redirectToAppeals] - Redirect to appeals frontend with id $penaltyId and is late payment penalty: $isLPP " +
        s"and cannot be appealed: $isFindOutHowToAppealLSP and is LPP2: $isLPP2")
      if (isFindOutHowToAppealLSP) {
        Future(Redirect(s"${appConfig.penaltiesAppealsBaseUrl}" +
          s"/initialise-appeal-against-the-obligation?penaltyId=$penaltyId"))
      } else {
        Future(Redirect(s"${appConfig.penaltiesAppealsBaseUrl}/initialise-appeal?penaltyId=$penaltyId&isLPP=$isLPP&isAdditional=$isLPP2"))
      }
    }


  def redirectToFindOutHowToAppealLPP(principalChargeReference: String,
                                      vatAmountInPence: Int,
                                      vatPeriodStartDate: String,
                                      vatPeriodEndDate: String,
                                      isCa: Boolean = false): Action[AnyContent] =
    authorise.async {
      logger.debug(s"[IndexController][redirectToFindOutHowToAppealLPP] - Redirect to appeals frontend with principleChargeReference: $principalChargeReference " +
        s"and has vatPeriodStartDate: $vatPeriodStartDate and has vatPeriodEndDate: $vatPeriodEndDate and has vatAmountInPence: $vatAmountInPence and is Ca: $isCa")
      Future(Redirect(s"${appConfig.penaltiesAppealsBaseUrl}/initialise-appeal-find-out-how-to-appeal?principalChargeReference=$principalChargeReference&vatAmountInPence=$vatAmountInPence&vatPeriodStartDate=$vatPeriodStartDate&vatPeriodEndDate=$vatPeriodEndDate&isCa=$isCa"))
    }
}
