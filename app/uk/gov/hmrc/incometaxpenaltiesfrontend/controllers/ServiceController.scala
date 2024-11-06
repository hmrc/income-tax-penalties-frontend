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

import uk.gov.hmrc.incometaxpenaltiesfrontend.views.html.IndividualMainView
import uk.gov.hmrc.incometaxpenaltiesfrontend.views.html.templates.SessionExpired
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.incometaxpenaltiesfrontend.config.AppConfig

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton
class ServiceController @Inject()(mcc: MessagesControllerComponents,
                                  individualMainView: IndividualMainView,
                                  sessionExpired: SessionExpired)(appConfig: AppConfig) extends FrontendController(mcc) {


  val individualMain: Action[AnyContent] = Action.async { implicit request =>
    Future.successful(Ok(individualMainView()))
  }

  val serviceSignout: Action[AnyContent] = Action.async { implicit request =>
    Future.successful(Ok(sessionExpired()).withNewSession)
  }

  val serviceSessionExpired: Action[AnyContent] = Action {
    Redirect(appConfig.survey).withNewSession
  }


  val keepAlive: Action[AnyContent] = Action {
    NoContent
  }
}


