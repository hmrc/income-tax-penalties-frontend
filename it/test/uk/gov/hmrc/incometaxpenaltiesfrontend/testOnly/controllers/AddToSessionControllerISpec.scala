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

package uk.gov.hmrc.incometaxpenaltiesfrontend.testOnly.controllers

import play.api.test.Helpers.LOCATION
import uk.gov.hmrc.incometaxpenaltiesfrontend.utils.{ComponentSpecHelper, SessionCookieCrumbler}

import java.time.LocalDate
import java.time.format.DateTimeFormatter

class AddToSessionControllerISpec extends ComponentSpecHelper {

  "GET /add-to-session" should {
    "add KV to session" in {
      val result = get("/test-only/add-to-session?key=Origin&value=PTA")

      result.status shouldBe 303
      result.header(LOCATION) shouldBe Some("/penalties/income-tax")
      SessionCookieCrumbler.getSessionMap(result).get("Origin") shouldBe Some("PTA")
    }
  }
}
