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

package uk.gov.hmrc.incometaxpenaltiesfrontend.models

import play.api.mvc.{Request, WrappedRequest}
import play.twirl.api.Html

case class CurrentUserRequest[A](mtdItId: String,
                                 arn: Option[String] = None,
                                 override val navBar: Option[Html] = None)(implicit request: Request[A]) extends WrappedRequest[A](request) with RequestWithNavBar {
  val isAgent: Boolean = arn.isDefined
}
