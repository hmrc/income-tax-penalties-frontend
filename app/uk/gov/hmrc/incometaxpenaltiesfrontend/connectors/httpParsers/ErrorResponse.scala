/*
 * Copyright 2023 HM Revenue & Customs
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

package uk.gov.hmrc.incometaxpenaltiesfrontend.connectors.httpParsers

import play.api.http.Status.BAD_REQUEST

sealed trait ErrorResponse {
  val status: Int
  val body: String
}

case object InvalidJson extends ErrorResponse {
  override val status: Int = BAD_REQUEST
  override val body: String = "Invalid JSON received"
}

case object BadRequest extends ErrorResponse {
  override val status: Int = BAD_REQUEST
  override val body: String = "Incorrect JSON body sent"
}

case class UnexpectedFailure(
                              override val status: Int,
                              override val body: String
                            ) extends ErrorResponse
