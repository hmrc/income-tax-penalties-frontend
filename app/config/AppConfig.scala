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
import uk.gov.hmrc.play.bootstrap.binders.RedirectUrl

import java.net.URLEncoder
import javax.inject.{Inject, Singleton}

@Singleton
class AppConfig @Inject()(config: Configuration) {
  val welshLanguageSupportEnabled: Boolean = config.getOptional[Boolean]("features.welsh-language-support").getOrElse(false)

  def languageMap: Map[String, Lang] = Map("en" -> Lang("en"), "cy" -> Lang("cy"))

  lazy val signInContinueBaseUrl: String = config.get[String]("signIn.continueBaseUrl")

  lazy val signInContinueUrl: String = URLEncoder.encode(RedirectUrl(signInContinueBaseUrl + controllers.routes.PenaltiesController.onPageLoad.url).unsafeValue, "UTF-8")

  lazy val signOutUrlUnauthorised: String = config.get[String]("signOut.url") + signInContinueUrl

  lazy val feedbackUrl: String = config.get[String]("urls.feedback")

  lazy val signOutUrl: String = config.get[String]("signOut.url") + feedbackUrl

}
