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

package uk.gov.hmrc.incometaxpenaltiesfrontend.controllers

import play.api.http.Status.{NO_CONTENT, SEE_OTHER}
import play.api.test.Helpers.LOCATION
import uk.gov.hmrc.incometaxpenaltiesfrontend.config.AppConfig

import java.net.URLEncoder

class ServiceControllerISpec extends ControllerISpecHelper {

  "GET /view-penalty/self-assessment/sign-out" should {
    "redirect to sign-out route with the continue URL set to the feedback survey" in {
      val appConfig = app.injector.instanceOf[AppConfig]
      stubAuthRequests(false)

      val result = get("/sign-out")

      val encodedContinueUrl = URLEncoder.encode(appConfig.survey, "UTF-8")
      val expectedRedirectUrl = s"${appConfig.signOutUrl}?continue=$encodedContinueUrl"

      result.status shouldBe SEE_OTHER
      result.header(LOCATION) shouldBe Some(expectedRedirectUrl)
    }
  }

  "GET /view-penalty/self-assessment/keep-alive" should {
    "return No-Content" in {
      stubAuthRequests(false)

      val result = get("/keep-alive")
      result.status shouldBe NO_CONTENT
    }
  }
}
