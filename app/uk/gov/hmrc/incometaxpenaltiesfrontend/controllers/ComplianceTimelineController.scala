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
import uk.gov.hmrc.incometaxpenaltiesfrontend.config.{AppConfig, ErrorHandler}
import uk.gov.hmrc.incometaxpenaltiesfrontend.controllers.auth.actions.AuthActions
import uk.gov.hmrc.incometaxpenaltiesfrontend.services.{ComplianceService, TimelineBuilderService}
import uk.gov.hmrc.incometaxpenaltiesfrontend.utils.Logger.logger
import uk.gov.hmrc.incometaxpenaltiesfrontend.views.html.ComplianceTimeline
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class ComplianceTimelineController @Inject()(override val controllerComponents: MessagesControllerComponents,
                                             complianceTimelineView: ComplianceTimeline,
                                             authActions: AuthActions,
                                             timelineBuilder: TimelineBuilderService,
                                             complianceService: ComplianceService,
                                             errorHandler: ErrorHandler
                                            )(implicit appConfig: AppConfig, ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def complianceTimelinePage(isAgent: Boolean): Action[AnyContent] =
    authActions.asMTDUser(isAgent).async { implicit currentUserRequest =>
      complianceService.calculateComplianceWindow() match {
        case Some((fromDate, toDate)) =>
          for {
            optComplianceData <- complianceService.getDESComplianceData(currentUserRequest.nino, fromDate, toDate)
            timelineEvents = timelineBuilder.buildTimeline(optComplianceData)
            result = Ok(complianceTimelineView(currentUserRequest.isAgent, timelineEvents, toDate))
          } yield result
        case _ =>
          logger.warn("[ComplianceTimelineController][complianceTimelinePage] - No compliance window calculated, rendering ISE")
          errorHandler.showInternalServerError()
      }
    }
}

