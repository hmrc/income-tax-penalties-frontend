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

package controllers

import config.AppConfig
import connectors.PenaltiesConnector
import controllers.actions.CombinedAction
import models.requests.IdentifierRequest
import play.api.Configuration
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, ActionBuilder, AnyContent, MessagesControllerComponents}
import services.LayoutService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class PenaltiesController @Inject()(
  view: views.html.Penalties,
  val penaltiesConnector: PenaltiesConnector,
  agentOrIndividual: CombinedAction,
  val mcc: MessagesControllerComponents,
  layoutService: LayoutService
)(implicit
  val ec: ExecutionContext,
  val config: Configuration,
  val appConfig: AppConfig
) extends FrontendController(mcc) with I18nSupport {

  extension (ab: ActionBuilder[IdentifierRequest,_]) private def summary = ab.async { implicit request =>
    val ninoEnrolmentKey = s"HMRC-PT~NINO~${request.clientNino}"
    for (penaltyDetails <- penaltiesConnector.getPenaltyDetails(ninoEnrolmentKey)) yield {
      val penalties = new models.Penalties(penaltyDetails)
      Ok(view(penalties, layoutService.layoutModel(pageTitle = "Self-Assessment Penalties")))
    }
  }

  def combinedSummary(): Action[AnyContent] = agentOrIndividual().summary

}
