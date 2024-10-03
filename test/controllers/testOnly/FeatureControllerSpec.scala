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

package controllers.testOnly

import base.SpecBase
import config.AppConfig
import play.api.libs.json.JsBoolean
import play.api.test.FakeRequest
import play.api.test.Helpers.*

class FeatureControllerSpec extends SpecBase {

  "Feature Controller" - {
    "must switch use session service feature" in {
      val application = applicationBuilder().configure("application.router" -> "testOnlyDoNotUseInAppConf.Routes").build()
      val config = application.injector.instanceOf[AppConfig]

      running(application) {
        config.featureUseSessionService = false

        val request = FakeRequest(PUT, routes.FeatureController.putUseSessionService().url).withBody(JsBoolean(true))

        val result = route(application, request).value

        status(result) mustEqual OK
        config.featureUseSessionService mustBe true
      }
    }

    "must switch optimise auth for individuals feature" in {
      val application = applicationBuilder().configure("application.router" -> "testOnlyDoNotUseInAppConf.Routes").build()
      val config = application.injector.instanceOf[AppConfig]

      running(application) {
        config.featureOptimiseAuthForIndividuals = true

        val request = FakeRequest(PUT, routes.FeatureController.putOptimiseAuthForIndividuals().url).withBody(JsBoolean(false))

        val result = route(application, request).value

        status(result) mustEqual OK
        config.featureOptimiseAuthForIndividuals mustBe false
      }
    }
  }
}
