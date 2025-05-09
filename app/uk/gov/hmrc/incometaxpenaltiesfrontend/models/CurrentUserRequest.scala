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

import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{Request, WrappedRequest}
import play.twirl.api.Html

abstract class CurrentUserRequest[A](request: Request[A]) extends WrappedRequest[A](request) with RequestWithNavBar {
  val mtdItId: String
  val arn: Option[String]
  val navBar: Option[Html]

  lazy val isAgent: Boolean = arn.isDefined

  lazy val auditJson: JsObject = Json.obj(
    "identifierType" -> "MTDITID",
    "taxIdentifier" -> mtdItId
  ) ++ arn.fold(Json.obj())(arn => Json.obj("agentReferenceNumber" -> arn))
}

case class AuthenticatedUserRequest[A](mtdItId: String,
                                       arn: Option[String] = None,
                                       navBar: Option[Html] = None)(implicit request: Request[A]) extends CurrentUserRequest[A](request) {
  def addNavBar(content: Html): AuthenticatedUserRequest[A] = copy(navBar = Some(content))
}

case class AuthenticatedUserWithPenaltyData[A](mtdItId: String,
                                            penaltyDetails: PenaltyDetails,
                                            arn: Option[String] = None,
                                            navBar: Option[Html])(implicit request: Request[A]) extends CurrentUserRequest[A](request)

