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

package uk.gov.hmrc.incometaxpenaltiesfrontend.models.compliance

import play.api.libs.json.{Json, OFormat}

case class MandationStatus(code: String, friendlyName: String) {
  // TODO 
  //  - confirm how/if this will be saved by V&C e.g are the fields named correctly? 
  //  - apparently the income tax session data store is only used for agents not individuals
  override def toString: String = friendlyName
}

object MandationStatus {
  implicit val format: OFormat[MandationStatus] = Json.format[MandationStatus]
}
