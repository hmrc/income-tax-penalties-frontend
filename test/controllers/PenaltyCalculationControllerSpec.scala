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

import config.AppConfig
import connectors.PenaltiesConnector
import connectors.PenaltiesConnector.GetPenaltyDetails
import controllers.actions.CombinedAction
import controllers.actions.{AgentAction, CombinedAction, FakeIdentifierAction, IdentifierAction}
import org.apache.pekko.util.Timeout
import org.mockito.ArgumentMatchers.{any, anyString}
import org.mockito.Mockito.when
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar.mock
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.Configuration
import play.api.mvc.{MessagesControllerComponents, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.await
import play.api.{Application, Configuration}
import services.LayoutService
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.{CompositeRetrieval, Retrieval, SimpleRetrieval}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future.{failed, successful}
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

class PenaltyCalculationControllerSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite {

  override def fakeApplication(): Application =
    new GuiceApplicationBuilder()
      .configure(
        "metrics.jvm" -> false,
        "metrics.enabled" -> false
      )
      .overrides(
        bind[IdentifierAction].to[FakeIdentifierAction]
      )
      .build()

  private val fakeRequest = FakeRequest("GET", "/")

  private val mockPenaltiesConnector = mock[PenaltiesConnector]
  
  private val controller = new PenaltyCalculationController(
    view = app.injector.instanceOf[views.html.PenaltyCalculation],
    penaltiesConnector = mockPenaltiesConnector,
    agentOrIndividual = app.injector.instanceOf[CombinedAction],
    mcc = app.injector.instanceOf[MessagesControllerComponents],
    layoutService = app.injector.instanceOf[LayoutService]
  )(ec = app.injector.instanceOf[ExecutionContext],
    config = app.injector.instanceOf[Configuration],
    appConfig = app.injector.instanceOf[AppConfig]
  )

  implicit val timeout: Timeout = Timeout(10 seconds)

  val noPenaltyDetails: GetPenaltyDetails = Json.parse("{}").as[GetPenaltyDetails]
  when(mockPenaltiesConnector.getPenaltyDetails(anyString())(any())).thenReturn(successful(noPenaltyDetails))

  "GET /" should {
    "return 200" in {
      val result: Result = await(controller.combinedSummary("")(fakeRequest))
      result.header.status shouldBe Status.OK
    }

    "return HTML" in {
      val result = await(controller.combinedSummary("")(fakeRequest))
      result.body.contentType shouldBe Some("text/html; charset=utf-8")
    }
  }

}
