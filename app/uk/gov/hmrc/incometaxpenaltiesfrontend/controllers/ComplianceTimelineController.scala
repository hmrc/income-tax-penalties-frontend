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

import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.incometaxpenaltiesfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesfrontend.controllers.auth.AuthenticatedController
import uk.gov.hmrc.incometaxpenaltiesfrontend.services.{ComplianceService, TimelineBuilderService}
import uk.gov.hmrc.incometaxpenaltiesfrontend.views.html.ComplianceTimeline

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class ComplianceTimelineController @Inject()(mcc: MessagesControllerComponents,
                                             complianceTimelineView: ComplianceTimeline,
                                             val authConnector: AuthConnector,
                                             timelineBuilder: TimelineBuilderService,
                                             complianceService: ComplianceService)
                                            (implicit appConfig: AppConfig, ec: ExecutionContext) extends AuthenticatedController(mcc) {



  val complianceTimelinePage: Action[AnyContent] = isAuthenticated {
    implicit request =>
    implicit currentUser =>
      for{
        optComplianceData <- complianceService.getDESComplianceData(currentUser.mtdItId)
        complianceData = optComplianceData.getOrElse(throw new InternalServerException("[ComplianceTimelineController][complianceTimelinePage] no available compliance data"))
        timelineEvents = timelineBuilder.buildTimeline(complianceData)
      } yield {Ok(complianceTimelineView(currentUser.isAgent, timelineEvents))}
  }

}


