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

package uk.gov.hmrc.incometaxpenaltiesfrontend.controllers

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{mock, when}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Configuration
import play.api.http.Status.BAD_REQUEST
import play.api.mvc.{MessagesControllerComponents, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.{NOT_FOUND, OK, contentAsString, defaultAwaitTimeout, status, stubMessagesControllerComponents}
import uk.gov.hmrc.incometaxpenaltiesfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesfrontend.featureswitch.frontend.controllers.FeatureSwitchFrontendController
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.concurrent.{ExecutionContext, Future}

class FeatureSwitchFrontendControllerISpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite{

  lazy val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  val mockConfig: Configuration = mock(classOf[Configuration])
  val mockServicesConfig: ServicesConfig = mock(classOf[ServicesConfig])
  val mcc: MessagesControllerComponents = stubMessagesControllerComponents()

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

  val controller = new FeatureSwitchFrontendController(
    featureSwitchService = mock(classOf[uk.gov.hmrc.incometaxpenaltiesfrontend.featureswitch.frontend.services.FeatureSwitchRetrievalService]),
    featureSwitchView = mock(classOf[uk.gov.hmrc.incometaxpenaltiesfrontend.featureswitch.frontend.views.html.feature_switch]),
    mcc = mcc

  )(ec, appConfig)


  "setTimeMachineDate" should {

    s"return $NOT_FOUND (NOT_FOUND) when the date provided is invalid" in {
      val result: Future[Result] = this.controller.setTimeMachineDate(Some("invalid date"))(FakeRequest())
      status(result) shouldBe BAD_REQUEST
      contentAsString(result) shouldBe "The date provided is in an invalid format"
    }


    s"return $OK (OK) when the date provided is valid" in {
      val timeMachineDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")

      val result: Future[Result] = this.controller.setTimeMachineDate(Some("01-01-2022"))(FakeRequest())
      status(result) shouldBe OK
      contentAsString(result) shouldBe s"Time machine set to: ${LocalDate.parse("01-01-2022" , timeMachineDateFormatter)}"

      sys.props.get("TIME_MACHINE_NOW").map(dateString => LocalDate.parse(dateString, timeMachineDateFormatter)) shouldBe Some(LocalDate.of(2022, 1, 1))
    }

    s"return $OK (OK) and the systems current date when no date is provided" in {
      when(mockConfig.getOptional[String](any())(any()))
        .thenReturn(None)
      val result: Future[Result] = this.controller.setTimeMachineDate(None)(FakeRequest())
      status(result) shouldBe OK
      contentAsString(result) shouldBe s"Time machine set to: ${LocalDate.now().toString}"
      this.controller.getFeatureDate(appConfig) shouldBe LocalDate.now()

    }
  }
}