/*
 * Copyright 2026 HM Revenue & Customs
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

import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.mvc.{ActionBuilder, AnyContent, BodyParser, MessagesControllerComponents, Request, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.stubMessagesControllerComponents
import uk.gov.hmrc.incometaxpenaltiesfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesfrontend.controllers.auth.actions.AuthActions
import uk.gov.hmrc.incometaxpenaltiesfrontend.controllers.auth.models.AuthenticatedUserWithPenaltyData
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.penaltyDetails.breathingSpace.BreathingSpace
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.penaltyDetails.lpp.{LPPDetails, LatePaymentPenalty}
import uk.gov.hmrc.incometaxpenaltiesfrontend.services.AuditService
import uk.gov.hmrc.incometaxpenaltiesfrontend.utils.TimeMachine
import uk.gov.hmrc.incometaxpenaltiesfrontend.views.html.{Lpp1Calculation, Lpp2Calculation}
import fixtures.*
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, eq as meq}
import org.mockito.Mockito.{reset, times, verify, verifyNoInteractions, when}
import play.twirl.api.Html
import uk.gov.hmrc.http.HeaderCarrier
import play.api.test.Helpers.*

import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}

class PenaltyCalculationControllerSpec extends AnyWordSpec with Matchers with MockitoSugar with BeforeAndAfterEach with GuiceOneAppPerSuite with LPPDetailsTestData with PenaltiesDetailsTestData {
  implicit val ec: ExecutionContext = ExecutionContext.global
  implicit val hc: HeaderCarrier = HeaderCarrier()
  lazy val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  val mockLpp1View: Lpp1Calculation = mock[Lpp1Calculation]
  val mockLpp2View: Lpp2Calculation = mock[Lpp2Calculation]
  val mockAuditService: AuditService = mock[AuditService]
  val mockTimeMachine: TimeMachine = mock[TimeMachine]
  val mockAuthActions: AuthActions = mock[AuthActions]
  val today: LocalDate = LocalDate.of(2024, 9, 24)
  val cc: MessagesControllerComponents = stubMessagesControllerComponents()

  val lpp1: LPPDetails = sampleUnpaidLPP1
  val lpp2: LPPDetails = sampleLPP2
  val lppManual: LPPDetails = sampleManualLPP

  def userRequest(lpps: Seq[LPPDetails], breathingSpace: Option[Seq[BreathingSpace]], isAgent: Boolean): AuthenticatedUserWithPenaltyData[AnyContent] = {
    AuthenticatedUserWithPenaltyData[AnyContent](
      mtdItId = "testMtdItId",
      nino = "AA123456A",
      penaltyDetails = samplePenaltyDetailsModel.copy(
        latePaymentPenalty = Some(LatePaymentPenalty(Some(lpps))),
        breathingSpace = breathingSpace),
      arn = if (isAgent) Some("testArn") else None,
      navBar = None
    )(FakeRequest())
  }

  def stubAuth(req: AuthenticatedUserWithPenaltyData[AnyContent], isAgent: Boolean): Unit = {
    when(mockAuthActions.asMTDUserWithPenaltyData(meq(isAgent)))
      .thenReturn(new ActionBuilder[AuthenticatedUserWithPenaltyData, AnyContent] {
        override def parser: BodyParser[AnyContent] = cc.parsers.defaultBodyParser

        override protected def executionContext: ExecutionContext = cc.executionContext

        override def invokeBlock[A](request: Request[A], block: AuthenticatedUserWithPenaltyData[A] => Future[Result]): Future[Result] = block(req.asInstanceOf[AuthenticatedUserWithPenaltyData[A]])
      }
      )
  }

  def controller: PenaltyCalculationController = new PenaltyCalculationController(
    controllerComponents = cc,
    lpp1CalculationView = mockLpp1View,
    lpp2CalculationView = mockLpp2View,
    authActions = mockAuthActions,
    auditService = mockAuditService
  )(appConfig, mockTimeMachine)

  def call(id: String,
           isAgent: Boolean = false,
           isLPP2: Boolean = false,
           lpps: Seq[LPPDetails] = Seq(lpp1), breathingSpace: Option[Seq[BreathingSpace]] = None): Future[Result] = {
    stubAuth(userRequest(lpps, breathingSpace, isAgent), isAgent)
    controller.penaltyCalculationPage(id, isAgent, isLPP2)(FakeRequest())
  }

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockLpp1View, mockLpp2View, mockAuditService, mockTimeMachine, mockAuthActions)

    when(mockTimeMachine.getCurrentDate())
      .thenReturn(today)
    when(mockLpp1View.apply(any(), any(), any(), any(), any())(any(), any(), any()))
      .thenReturn(Html("lpp1"))
    when(mockLpp2View.apply(any(), any(), any(), any(), any())(any(), any(), any()))
      .thenReturn(Html("lpp2"))
  }

  "is Correct LPP" should {
    "be true for LPP2 when isLPP2 is true" in {
      controller.isCorrectLPP(lpp2, isLPP2 = true) shouldBe true
    }
    "be false for LPP1 when isLPP2 is true" in {
      controller.isCorrectLPP(lpp1, isLPP2 = true) shouldBe false
    }
    "be true for LPP1 when isLPP2 is false" in {
      controller.isCorrectLPP(lpp1, isLPP2 = false) shouldBe true
    }
    "be false for LPP2 when isLPP2 is false" in {
      controller.isCorrectLPP(lpp2, isLPP2 = false) shouldBe false
    }
    "be false for MANUAL in either mode" in {
      controller.isCorrectLPP(lppManual, isLPP2 = true) shouldBe false
      controller.isCorrectLPP(lppManual, isLPP2 = false) shouldBe false
    }
  }
  "PenaltyCalculationPage" should {
    "render the LPP1 view and audit when an LPP1 matches" in {
      val result = call(lpp1.principalChargeReference)
      status(result) shouldBe OK
      contentAsString(result) shouldBe "lpp1"
      verify(mockLpp1View).apply(any(), meq(false), any(), any(), any())(any(), any(), any())
      verifyNoInteractions(mockLpp2View)
      verify(mockAuditService, times(1)).audit(any())(any())
    }
    "pass isAgent = true through to the view for agents" in {
      status(call(lpp1.principalChargeReference, isAgent = true)) shouldBe OK
      verify(mockLpp1View).apply(any(), meq(true), any(), any(), any())(any(), any(), any())
    }
    "render the LPP2 view and audit when an LPP2 matches" in {
      val result = call(lpp2.principalChargeReference, isLPP2 = true, lpps = Seq(lpp2))
      status(result) shouldBe OK
      contentAsString(result) shouldBe "lpp2"
      verifyNoInteractions(mockLpp1View)
      verify(mockAuditService, times(1)).audit(any())(any())
    }
    "redirect home and not audit when no penalty has the given id" in {
      val result = call("UNKOWN")
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.IndexController.homePage(false).url)
      verifyNoInteractions(mockAuditService, mockLpp1View, mockLpp2View)
    }
    "redirect to the agent home page for agents" in {
      val result = call("UNKOWN", isAgent = true)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.IndexController.homePage(true).url)
    }
    "redirect when a LPP1 id is requested as LPP2" in {
      val result = call(lpp1.principalChargeReference, isLPP2 = true)
      status(result) shouldBe SEE_OTHER
      verifyNoInteractions(mockAuditService)
    }
    "redirect when an LPP2 id is requested as LPP1" in {
      val result = call(lpp2.principalChargeReference, lpps = Seq(lpp2))
      status(result) shouldBe SEE_OTHER
      verifyNoInteractions(mockAuditService)
    }
    "redirect when there are no late payment penalties" in {
      status(call(lpp1.principalChargeReference, lpps = Seq.empty)) shouldBe SEE_OTHER
    }
  }
  "penaltyCalculationPage Breathing Space" should {
    def isInBreathingSpace(bs: Option[Seq[BreathingSpace]]): Boolean = {
      val captor = ArgumentCaptor.forClass(classOf[java.lang.Boolean])
      status(call(lpp1.principalChargeReference, breathingSpace = bs)) shouldBe OK
      verify(mockLpp1View).apply(any(), any(), any(), captor.capture(), any())(any(), any(), any())
      captor.getValue.booleanValue()
    }

    def period(start: LocalDate, end: LocalDate) = Some(Seq(BreathingSpace(bsStartDate = start, bsEndDate = end)))

    "be false when there is none" in {
      isInBreathingSpace(None) shouldBe false
    }
    "be true when today is within the period" in {
      isInBreathingSpace(period(today.minusDays(5), today.plusDays(5))) shouldBe true
    }
    "be true in the start date" in {
      isInBreathingSpace(period(today, today.plusDays(5))) shouldBe true
    }
    "be true on the end date" in {
      isInBreathingSpace(period(today.minusDays(5), today)) shouldBe true
    }
    "be false before the period starts" in {
      isInBreathingSpace(period(today.plusDays(1), today.plusDays(9))) shouldBe false
    }
    "be false after the period ends" in {
      isInBreathingSpace(period(today.minusDays(9), today.minusDays(1))) shouldBe false
    }
    "be true when any one of several periods covers today" in {
      val periods = Some(Seq(
        BreathingSpace(today.minusDays(30), today.minusDays(20)),
        BreathingSpace(today.minusDays(1), today.plusDays(1))
      ))
      isInBreathingSpace(periods) shouldBe true
    }
  }
}
