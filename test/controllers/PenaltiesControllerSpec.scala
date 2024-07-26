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

package controllers

import org.apache.pekko.util.Timeout
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.http.Status
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers.await

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

class PenaltiesControllerSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite {
  override def fakeApplication(): Application =
    new GuiceApplicationBuilder()
      .configure(
        "metrics.jvm"     -> false,
        "metrics.enabled" -> false
      )
      .build()

  private val fakeRequest = FakeRequest("GET", "/")

  private val controller = app.injector.instanceOf[PenaltiesController]

  implicit val timeout: Timeout = Timeout(10 seconds)

  "GET /" should {
    "return 200" in {
      val result: Result = await(controller.onPageLoad(fakeRequest))
      result.header.status shouldBe Status.OK
    }

    "return HTML" in {
      val result = await(controller.onPageLoad(fakeRequest))
      result.body.contentType shouldBe Some("text/html; charset=utf-8")
    }
  }
}
