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

package controllers.testOnly

import config.AppConfig
import connectors.SessionDataConnector
import play.api.Logging
import play.api.libs.json.Reads
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class FeatureController @Inject()(
  val mcc: MessagesControllerComponents,
  appConfig: AppConfig
)(implicit
  val ec: ExecutionContext
) extends FrontendController(mcc) with Logging {

  import appConfig.*

  private def put(fun: Boolean => Unit): Action[AnyContent] = Action { request =>
    val data = request.body.asJson.get
    val flag = data.as[Boolean]
    fun(flag)
    Ok
  }

  def putUseSessionService (): Action[AnyContent] = put(appConfig.featureUseSessionService = _)

  def putOptimiseAuthForIndividuals (): Action[AnyContent] = put(appConfig.featureOptimiseAuthForIndividuals = _)

}
