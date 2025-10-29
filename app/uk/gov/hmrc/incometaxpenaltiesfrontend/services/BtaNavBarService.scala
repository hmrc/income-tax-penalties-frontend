/*
 * Copyright 2025 HM Revenue & Customs
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

package uk.gov.hmrc.incometaxpenaltiesfrontend.services

import play.api.i18n.Messages
import play.twirl.api.Html
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.incometaxpenaltiesfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesfrontend.connectors.BtaNavLinksConnector
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.btaNavBar.{ListLink, NavLink}
import uk.gov.hmrc.incometaxpenaltiesfrontend.utils.NotificationBadgeCountUtil.notificationBadgeCount
import uk.gov.hmrc.incometaxpenaltiesfrontend.views.html.navBar.BtaNavBar

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class BtaNavBarService @Inject()(btaNavLinksConnector: BtaNavLinksConnector,
                                 btaNavBar: BtaNavBar)
                                (implicit val appConfig: AppConfig) {

  def retrieveBtaLinksAndRenderNavBar()(implicit headerCarrier: HeaderCarrier, ec: ExecutionContext, messages: Messages): Future[Option[Html]] = {
    btaNavLinksConnector.getBtaNavLinks().map(_.map { content =>
      btaNavBar(
        Seq(
          Some(ListLink(content.home.message, content.home.url)),
          Some(ListLink(content.account.message, content.account.url)),
          Some(ListLink(content.messages.message, content.messages.url, Some(notificationBadgeCount(content.messages.alerts.getOrElse(0))))),
          formsNav(content.forms),
          Some(ListLink(content.help.message, content.help.url))
        ).flatten
      )
    })
  }

  private[services] def formsNav(form: NavLink)(implicit messages: Messages): Option[ListLink] =
    form.alerts.collect { case alert if alert > 0 =>
      ListLink(form.message, form.url, Some(notificationBadgeCount(alert)))
    }

}
