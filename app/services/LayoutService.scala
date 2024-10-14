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

package services

import play.api.i18n.Messages
import play.api.mvc.Request
import uk.gov.hmrc.hmrcfrontend.config.AccountMenuConfig
import uk.gov.hmrc.hmrcfrontend.views.Implicits._
import uk.gov.hmrc.hmrcfrontend.views.viewmodels.accountmenu.AccountMenu

import javax.inject._

object LayoutService {
  case class LayoutModel(
    pageTitle: String,
    accountMenu: AccountMenu,
    showSignOut: Boolean = true
  ) {
    def withoutSignOut: LayoutModel = this.copy(showSignOut = false)
  }
}

@Singleton
class LayoutService @Inject()(implicit accountMenuConfig: AccountMenuConfig) {
  import LayoutService._

  def layoutModel(pageTitle: String)(implicit request: Request[_], messages: Messages): LayoutModel =
    LayoutModel(pageTitle = pageTitle, accountMenu = AccountMenu().withUrlsFromConfig())
}
