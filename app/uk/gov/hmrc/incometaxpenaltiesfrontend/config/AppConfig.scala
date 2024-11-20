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

package uk.gov.hmrc.incometaxpenaltiesfrontend.config

import javax.inject.{Inject, Singleton}
import play.api.Configuration
import uk.gov.hmrc.incometaxpenaltiesfrontend.config.featureSwitches.FeatureSwitch.FeatureSwitch
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import java.net.URLEncoder
import uk.gov.hmrc.play.bootstrap.binders.RedirectUrl

@Singleton
class AppConfig @Inject()(config: Configuration, servicesConfig: ServicesConfig) {
  val welshLanguageSupportEnabled: Boolean = config.getOptional[Boolean]("features.welsh-language-support").getOrElse(false)

  def getFeatureSwitchValue(feature: String): Boolean = config.get[Boolean](feature)

  def selfUrl: String = servicesConfig.baseUrl("income-tax-penalties-frontend")

  lazy val ITSAPenaltiesHomeUrl = "/penalties/income-tax"

  lazy val surveyOrigin: String =
    servicesConfig.getString("exit-survey-origin")
  lazy val survey = s"""${servicesConfig.getString("feedback-frontend-host")}/feedback/$surveyOrigin"""

  lazy val alphaBannerUrl = servicesConfig.getString("alpha-banner-url")

  def isFeatureSwitchEnabled(featureSwitch: FeatureSwitch): Boolean = config.get[Boolean](featureSwitch.name)

  lazy val featureTimeNow = servicesConfig.getString("feature.switch.time-machine-now")

  lazy val penaltiesUrl: String = s"${servicesConfig.baseUrl("income-tax-penalties-frontend")}/penalties"

  lazy val signInUrl: String = config.get[String]("signIn.url")

  val vatAgentClientLookupFrontendHost: String = "vat-agent-client-lookup-frontend.host"
  val vatAgentClientLookupFrontendStartUrl: String = "vat-agent-client-lookup-frontend.startUrl"
  private lazy val platformHost = servicesConfig.getString("host")

  private lazy val agentClientLookupRedirectUrl: String => String = uri => URLEncoder.encode(RedirectUrl(platformHost + uri).unsafeValue, "UTF-8")

  private lazy val agentClientLookupHost = servicesConfig.getConfString(vatAgentClientLookupFrontendHost, "")

  lazy val agentClientLookupStartUrl: String => String = (uri: String) =>
    agentClientLookupHost +
      servicesConfig.getConfString(vatAgentClientLookupFrontendStartUrl, "") +
      s"?redirectUrl=${agentClientLookupRedirectUrl(uri)}"

  lazy val penaltiesAppealsBaseUrl: String = config.get[String]("urls.penaltiesAppealsBaseurl") + "/penalties-appeals"



}
