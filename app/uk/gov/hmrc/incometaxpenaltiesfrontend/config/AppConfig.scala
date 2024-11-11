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
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

@Singleton
class AppConfig @Inject()(config: Configuration, servicesConfig: ServicesConfig) {
  val welshLanguageSupportEnabled: Boolean = config.getOptional[Boolean]("features.welsh-language-support").getOrElse(false)

  def getFeatureSwitchValue(feature: String): Boolean = config.get[Boolean](feature)

  def selfUrl: String = servicesConfig.baseUrl("income-tax-penalties-frontend")

  lazy val ITSAPenaltiesHomeUrl = "/penalties/income-tax"

  lazy val surveyOrigin: String =
    servicesConfig.getString("sca-wrapper.exit-survey-origin")
  val survey = s"""${servicesConfig.getString("sca-wrapper.feedback-frontend-host")}/feedback/$surveyOrigin"""

  val sessionTimeoutInSeconds = servicesConfig.getString("timeout.session-timeout-seconds")
  val sessionCountdownInSeconds = servicesConfig.getString("timeout.session-countdown-seconds")

  val alphaBannerUrl = servicesConfig.getString("alpha-banner-url")


  lazy val accessibilityBaseUrl: String = servicesConfig.getString("accessibility-statement.baseUrl")
  lazy private val accessibilityRedirectUrl: String = servicesConfig.getString("accessibility-statement.redirectUrl")

  lazy val plaftormFrontendUrl: String = servicesConfig.getConfString("platform.frontend.host", "")

  def accessibilityStatementUrl(referrer: String): String =
    s"$accessibilityBaseUrl/accessibility-statement$accessibilityRedirectUrl?referrerUrl=${plaftormFrontendUrl + referrer}"


}
