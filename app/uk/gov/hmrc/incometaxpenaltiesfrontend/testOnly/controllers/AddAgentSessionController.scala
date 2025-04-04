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

package uk.gov.hmrc.incometaxpenaltiesfrontend.testOnly.controllers

import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.incometaxpenaltiesfrontend.controllers.{routes => mainRoutes}
import uk.gov.hmrc.incometaxpenaltiesfrontend.utils.IncomeTaxSessionKeys
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import java.time.LocalDate
import java.time.format.DateTimeFormatter._
import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton
class AddAgentSessionController @Inject()(mcc: MessagesControllerComponents) extends FrontendController(mcc) {

  def addAgentSession(mtdItId: String): Action[AnyContent] = Action.async {
    implicit request =>
      val session = request.session + (IncomeTaxSessionKeys.pocAchievementDate -> LocalDate.now().format(ISO_DATE)) + (IncomeTaxSessionKeys.agentSessionMtditid -> mtdItId)
      Future.successful(SeeOther(mainRoutes.IndexController.homePage.url).withSession(session))
  }

}
