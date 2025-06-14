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
import uk.gov.hmrc.incometaxpenaltiesfrontend.config.AppConfig
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton
class ServiceController @Inject()(override val controllerComponents: MessagesControllerComponents
                                 )(implicit appConfig: AppConfig) extends FrontendBaseController with I18nSupport {

  val signOut: Action[AnyContent] = Action.async {
    Future.successful(
      Redirect(appConfig.signOutUrl, Map("continue" -> Seq(appConfig.survey)))
    )
  }

  val keepAlive: Action[AnyContent] = Action.async { _ => Future.successful(NoContent) }

}


