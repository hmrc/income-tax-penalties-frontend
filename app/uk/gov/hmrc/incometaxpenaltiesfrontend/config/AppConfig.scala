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

import play.api.Configuration
import uk.gov.hmrc.incometaxpenaltiesfrontend.featureswitch.core.config.{FeatureSwitching, UseStubForBackend}
import uk.gov.hmrc.play.bootstrap.binders.RedirectUrl
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import java.net.URLEncoder
import javax.inject.{Inject, Singleton}

@Singleton
class AppConfig @Inject()(config: Configuration, servicesConfig: ServicesConfig) extends FeatureSwitching {

  val appConfig: AppConfig = this

  val welshLanguageSupportEnabled: Boolean = config.getOptional[Boolean]("features.welsh-language-support").getOrElse(false)

  def getFeatureSwitchValue(feature: String): Boolean = config.get[Boolean](feature)

  def selfUrl: String = servicesConfig.baseUrl("income-tax-penalties-frontend")

  lazy val ITSAPenaltiesHomeUrl = "/penalties/income-tax"

  lazy val surveyOrigin: String = servicesConfig.getString("exit-survey-origin")
  lazy val survey = s"""${servicesConfig.getString("feedback-frontend-host")}/feedback/$surveyOrigin"""

  lazy val alphaBannerUrl = servicesConfig.getString("alpha-banner-url")

  def penaltiesUrl: String =
    if (isEnabled(UseStubForBackend)) s"${servicesConfig.baseUrl("income-tax-penalties-stubs")}/income-tax-penalties-stubs"
    else s"${servicesConfig.baseUrl("penalties")}/penalties"

  def messagesFrontendBaseUrl: String =
    if (isEnabled(UseStubForBackend)) s"${servicesConfig.baseUrl("income-tax-penalties-stubs")}/income-tax-penalties-stubs"
    else servicesConfig.baseUrl("message-frontend")

  def btaBaseUrl: String = servicesConfig.baseUrl("business-tax-account")

  lazy val signInUrl: String = config.get[String]("signIn.url")
  lazy val signOutUrl: String = config.get[String]("signOut.url")

  val vatAgentClientLookupFrontendHost: String = "vat-agent-client-lookup-frontend.host"
  val vatAgentClientLookupFrontendStartUrl: String = "vat-agent-client-lookup-frontend.startUrl"
  private lazy val platformHost = servicesConfig.getString("host")

  private lazy val agentClientLookupRedirectUrl: String => String = uri => URLEncoder.encode(RedirectUrl(platformHost + uri).unsafeValue, "UTF-8")

  private lazy val agentClientLookupHost = servicesConfig.getConfString(vatAgentClientLookupFrontendHost, "")

  lazy val agentClientLookupStartUrl: String => String = (uri: String) =>
    agentClientLookupHost +
      servicesConfig.getConfString(vatAgentClientLookupFrontendStartUrl, "") +
      s"?redirectUrl=${agentClientLookupRedirectUrl(uri)}"

  lazy val incomeTaxPenaltiesAppealsBaseUrl: String = config.get[String]("urls.incomeTaxPenaltiesAppealsBaseUrl") + "/penalties-appeals/income-tax"


}
