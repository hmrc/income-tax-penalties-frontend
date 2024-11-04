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

package uk.gov.hmrc.incometaxpenaltiesfrontend.views.templates

import com.google.inject.ImplementedBy
import play.api.Logging
import play.api.i18n.Messages
import play.api.mvc.Request
import play.twirl.api.{Html, HtmlFormat}
import uk.gov.hmrc.hmrcfrontend.views.viewmodels.hmrcstandardpage.ServiceURLs
import uk.gov.hmrc.incometaxpenaltiesfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesfrontend.views.html.components.HeadBlock
import uk.gov.hmrc.sca.models.BannerConfig
import uk.gov.hmrc.sca.services.WrapperService

import javax.inject.Inject
import scala.util.{Failure, Success, Try}


@ImplementedBy(classOf[MainTemplateImpl])
trait MainTemplate {
  def apply(
             pageTitle: String,
             sidebarLinks: Option[Html] = None,
             sidebarClass: Option[String] = None,
             disableSessionExpired: Boolean = false,
             fullWidth: Boolean = false,
             signOutUrl: Option[String] = None
           )(contentBlock: Html)(implicit
                                 request: Request[_],
                                 messages: Messages
           ): HtmlFormat.Appendable
}

class MainTemplateImpl @Inject() (
                                   appConfig: AppConfig,
                                   wrapperService: WrapperService,
                                   headBlock: HeadBlock
                                 ) extends MainTemplate
  with Logging {
  override def apply(
                      pageTitle: String,
                      sidebarLinks: Option[Html] = None,
                      sidebarClass: Option[String] = None,
                      disableSessionExpired: Boolean = false,
                      fullWidth: Boolean = false,
                      signOutUrl: Option[String] = None
                    )(
                      contentBlock: Html
                    )(implicit request: Request[_], messages: Messages): HtmlFormat.Appendable = {

    val fullPageTitle = s"$pageTitle - ${Messages("label.service_name")} - GOV.UK"


    wrapperService.standardScaLayout(
      content = contentBlock,
      pageTitle = Some(fullPageTitle),
      serviceURLs = ServiceURLs(
        serviceUrl = Some(appConfig.ITSAPenaltiesHomeUrl),
        signOutUrl = Some(uk.gov.hmrc.incometaxpenaltiesfrontend.controllers.routes.ServiceController.serviceSignout().url),
        accessibilityStatementUrl = Some(appConfig.accessibilityStatementUrl(request.uri))
      ),
      serviceNameKey = Some(messages("label.service_name")),
      sidebarContent = None,
      timeOutUrl = Some(uk.gov.hmrc.incometaxpenaltiesfrontend.controllers.routes.ServiceController.serviceSessionExpired().url),
      keepAliveUrl = uk.gov.hmrc.incometaxpenaltiesfrontend.controllers.routes.ServiceController.keepAlive().url,
      showBackLinkJS = true,
      styleSheets = Seq(headBlock()),
      bannerConfig = BannerConfig(
        showAlphaBanner = true,
        showBetaBanner = false,
        showHelpImproveBanner = false
      ),
      optTrustedHelper = None,
      fullWidth = fullWidth,
      disableSessionExpired = disableSessionExpired
    )(messages, request)
  }
}
