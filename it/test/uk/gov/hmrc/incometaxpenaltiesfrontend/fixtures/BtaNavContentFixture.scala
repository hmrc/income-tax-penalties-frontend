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

package uk.gov.hmrc.incometaxpenaltiesfrontend.fixtures

import uk.gov.hmrc.incometaxpenaltiesfrontend.models.btaNavBar.{NavContent, NavLink}

trait BtaNavContentFixture {

  val btaNavLink: NavLink = NavLink(
    en = "Foo",
    cy = "Bar",
    url = "/url",
    alerts = Some(0)
  )

  val btaNavContent: NavContent = NavContent(
    home = btaNavLink,
    account = btaNavLink,
    messages = btaNavLink,
    help = btaNavLink,
    forms = btaNavLink
  )

}
