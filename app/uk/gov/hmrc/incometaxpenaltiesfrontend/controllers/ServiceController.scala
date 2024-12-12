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
import uk.gov.hmrc.incometaxpenaltiesfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesfrontend.controllers.auth.AuthenticatedController
import uk.gov.hmrc.incometaxpenaltiesfrontend.utils.IncomeTaxSessionKeys
import uk.gov.hmrc.incometaxpenaltiesfrontend.views.html.IndexView

import java.time.LocalDate
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ServiceController @Inject()(val authConnector: AuthConnector,
                                  mcc: MessagesControllerComponents,
                                  indexView: IndexView
                                 )(implicit appConfig: AppConfig,
                                   ec: ExecutionContext) extends AuthenticatedController(mcc) {


  val homePage: Action[AnyContent] = isAuthenticated {
    implicit request =>
      implicit currentUser =>

        Future.successful(
          Ok(indexView(currentUser.isAgent))
            .addingToSession(IncomeTaxSessionKeys.pocAchievementDate -> LocalDate.now().toString)
        )
  }

  val serviceSessionExpired: Action[AnyContent] = isAuthenticated {
    implicit request =>
      implicit currentUser =>
        Future.successful(Redirect(appConfig.survey).withNewSession)
  }

  val keepAlive: Action[AnyContent] = isAuthenticated {
    implicit request =>
      implicit currentUser =>
        Future.successful(NoContent)
  }

}


