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
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.incometaxpenaltiesfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesfrontend.controllers.predicates.{AuthAction, NavBarRetrievalAction}
import uk.gov.hmrc.incometaxpenaltiesfrontend.services.{ComplianceService, TimelineBuilderService}
import uk.gov.hmrc.incometaxpenaltiesfrontend.views.html.ComplianceTimeline
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class ComplianceTimelineController @Inject()(override val controllerComponents: MessagesControllerComponents,
                                             complianceTimelineView: ComplianceTimeline,
                                             authorised: AuthAction,
                                             withNavBar: NavBarRetrievalAction,
                                             timelineBuilder: TimelineBuilderService,
                                             complianceService: ComplianceService)
                                            (implicit appConfig: AppConfig, ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val complianceTimelinePage: Action[AnyContent] =
    (authorised andThen withNavBar).async { implicit currentUserRequest =>
      for{
        optComplianceData <- complianceService.getDESComplianceData(currentUserRequest.mtdItId)
        complianceData = optComplianceData.getOrElse(throw new InternalServerException("[ComplianceTimelineController][complianceTimelinePage] no available compliance data"))
        timelineEvents = timelineBuilder.buildTimeline(complianceData)
      } yield {Ok(complianceTimelineView(currentUserRequest.isAgent, timelineEvents))}
  }

}


