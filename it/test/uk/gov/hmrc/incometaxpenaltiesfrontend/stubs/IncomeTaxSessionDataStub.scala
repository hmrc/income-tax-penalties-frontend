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

package uk.gov.hmrc.incometaxpenaltiesfrontend.stubs

import play.api.http.Status.OK
import uk.gov.hmrc.incometaxpenaltiesfrontend.controllers.auth.models.SessionData
import uk.gov.hmrc.incometaxpenaltiesfrontend.utils.WiremockMethods

trait IncomeTaxSessionDataStub extends WiremockMethods {

  val incomeTaxSessionDataUrl = "/income-tax-session-data"

  val testAgentMtditId = "1234567890"
  val testAgentUtr = "9999912345"

  val sessionData: String => SessionData = nino => SessionData(
    mtditid = testAgentMtditId,
    nino = nino,
    utr = testAgentUtr
  )

  def stubGetIncomeTaxSessionDataSuccessResponse[T](nino: String): Unit =
    when(method = GET, uri = incomeTaxSessionDataUrl)
      .thenReturn(status = OK, body = sessionData(nino))(SessionData.fmt)

}
