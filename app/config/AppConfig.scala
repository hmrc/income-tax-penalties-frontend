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

package config

import play.api.Configuration
import play.api.i18n.Lang
import utils.ServiceEndpoint

import java.net.URL
import javax.inject.{Inject, Singleton}

@Singleton
class AppConfig @Inject()(configuration: Configuration) {
  private def service: String => ServiceEndpoint = ServiceEndpoint(configuration)

  val penaltiesService: ServiceEndpoint = service("penalties")
  val feedbackFrontend: ServiceEndpoint = service("feedback-frontend")

  //  val host: String    = configuration.get[String]("host")
//  val appName: String = configuration.get[String]("appName")

//  private val contactHost = configuration.get[String]("contact-frontend.host")
//  private val contactFormServiceIdentifier = "penalties-admin-frontend"
//
//  def feedbackUrl(implicit request: RequestHeader): String =
//    s"$contactHost/contact/beta-feedback?service=$contactFormServiceIdentifier&backUrl=${host + request.uri}"

  val loginUrl: String         = configuration.get[String]("urls.login")
  val loginContinueUrl: String = configuration.get[String]("urls.loginContinue")
  val signOutUrl: String       = configuration.get[String]("urls.signOut")

  //private val exitSurveyBaseUrl: String = configuration.get[Service]("microservice.services.feedback-frontend").baseUrl
  val exitSurveyUrl: URL       = feedbackFrontend.resolve(s"/feedback/penalties-admin-frontend")

  val languageMap: Map[String, Lang] = {
    val languageCodes: Seq[String] = configuration.getOptional[Seq[String]]("play.i18n.langs").getOrElse(Seq("en"))
    languageCodes.map(lc => lc -> Lang(lc)).toMap
  }

  val languageTranslationEnabled: Boolean =
    configuration.getOptional[Boolean]("features.welsh-translation").getOrElse(languageMap.size > 1)

}