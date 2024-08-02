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

import config.AppConfig.ServiceEndpoint
import play.api.Configuration
import play.api.i18n.Lang

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

  // TODO: load this from "play.i18n.langs"
  val languageMap: Map[String, Lang] = Map(
    "en" -> Lang("en"),
    "cy" -> Lang("cy")
  )

  val languageTranslationEnabled: Boolean =
    configuration.getOptional[Boolean]("features.welsh-translation").getOrElse(languageMap.size > 1)

}

object AppConfig {
  case class ServiceEndpoint(protocol: String, host: String, port: Option[Int], prefix: String) {
    private def path = Some(prefix).filterNot(_.isBlank).map(_.stripSuffix("/")+"/").getOrElse("")
    private def portSpec = port.map(port => s":$port").getOrElse("")
    private val urlBase = s"$protocol://$host$portSpec/$path"
    def resolve(suffix: String): URL = new URL(urlBase + suffix.stripPrefix("/"))
  }
  object ServiceEndpoint {
    def apply(cfg: Configuration)(service: String) = new ServiceEndpoint(
      cfg.getOptional[String](s"microservice.services.$service.protocol").getOrElse("http"),
      cfg.getOptional[String](s"microservice.services.$service.host").getOrElse("localhost"),
      cfg.getOptional[Int](s"microservice.services.$service.port"),
      cfg.getOptional[String](s"microservice.services.$service.prefix").getOrElse(service)
    )
  }
}