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

package config

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.test.FakeRequest
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.incometaxpenaltiesfrontend.config.ErrorHandler

class ErrorHandlerSpec extends AnyWordSpec
  with Matchers
  with GuiceOneAppPerSuite
  with ScalaFutures {

  override def fakeApplication(): Application =
    new GuiceApplicationBuilder()
      .build()

  private val fakeRequest = FakeRequest("GET", "/")

  private val handler = app.injector.instanceOf[ErrorHandler]

  "standardErrorTemplate" should {
    "render HTML" in {
      val html = handler.standardErrorTemplate("title", "heading", "message")(fakeRequest).futureValue
      html.contentType shouldBe "text/html"
    }
  }

}
