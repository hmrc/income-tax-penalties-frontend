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

import play.api.mvc.{ActionBuilder, AnyContent, AnyContentAsEmpty, BodyParser, Request, Result}
import play.api.mvc.Results.InternalServerError as InternalServerErrorResult
import play.api.i18n.MessagesApi
import org.scalatest.matchers.should
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.test.FakeRequest
import uk.gov.hmrc.incometaxpenaltiesfrontend.config.AppConfig
import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}
import org.mockito.Mockito.verify
import play.api.test.Helpers.*
import uk.gov.hmrc.incometaxpenaltiesfrontend.connectors.mocks.{AuthMocks, IncomeTaxSessionMocks}
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.compliance.ComplianceData
import uk.gov.hmrc.incometaxpenaltiesfrontend.services.{AuditService, ComplianceService, TimelineBuilderService}
import uk.gov.hmrc.incometaxpenaltiesfrontend.config.ErrorHandler
import uk.gov.hmrc.incometaxpenaltiesfrontend.views.html.ComplianceTimeline
import uk.gov.hmrc.incometaxpenaltiesfrontend.controllers.auth.actions.AuthActions
import org.mockito.ArgumentMatchers.{any, eq as meq}
import org.mockito.Mockito.{reset, times, verifyNoInteractions, when}
import org.scalatest.BeforeAndAfterEach
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.audit.UserComplianceInfoAuditModel
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.compliance.ComplianceStatusEnum
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.compliance.ObligationDetail
import uk.gov.hmrc.incometaxpenaltiesfrontend.controllers.auth.models.{AuthorisedAndEnrolledAgent, AuthorisedAndEnrolledIndividual, CurrentUserRequest, SessionData}
import org.scalatestplus.mockito.MockitoSugar.{mock => mockitoMock}


class ComplianceTimelineControllerSpec extends AnyWordSpec with should.Matchers with GuiceOneAppPerSuite with AuthMocks with IncomeTaxSessionMocks with BeforeAndAfterEach {

  implicit val ec: ExecutionContext = ExecutionContext.global
  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit lazy val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  lazy val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  val mockComplianceService: ComplianceService = mockitoMock[ComplianceService]
  val mockTimelineBuilder: TimelineBuilderService = mockitoMock[TimelineBuilderService]
  val mockAuthActions: AuthActions = mockitoMock[AuthActions]
  val mockErrorHandler: ErrorHandler = mockitoMock[ErrorHandler]
  val mockAuditService: AuditService = mockitoMock[AuditService]
  val complianceTimelineView: ComplianceTimeline = app.injector.instanceOf[ComplianceTimeline]
  val fromDate: LocalDate = LocalDate.of(2023, 1, 1)
  val toDate: LocalDate = LocalDate.of(2023, 4, 5)
  override val testNino: String = "AA123456A"
  private val mcc = stubMessagesControllerComponents()


  def controller(): ComplianceTimelineController = new ComplianceTimelineController(
    controllerComponents = mcc,
    complianceTimelineView = complianceTimelineView,
    authActions = mockAuthActions,
    timelineBuilder = mockTimelineBuilder,
    complianceService = mockComplianceService,
    auditService = mockAuditService,
    errorHandler = mockErrorHandler,
  )(appConfig, ec)

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockComplianceService, mockTimelineBuilder, mockAuditService, mockErrorHandler)
  }

  private def stubAsMTDUser(isAgent: Boolean, currentUserRequest: CurrentUserRequest[_]): Unit = {
    when(mockAuthActions.asMTDUser(meq(isAgent))).thenReturn(
      new ActionBuilder[CurrentUserRequest, AnyContent] {
        override def parser: BodyParser[AnyContent] = mcc.parsers.defaultBodyParser

        override protected def executionContext: ExecutionContext = mcc.executionContext

        override def invokeBlock[A](req: Request[A], block: CurrentUserRequest[A] => Future[Result]): Future[Result] =
          block(currentUserRequest.asInstanceOf[CurrentUserRequest[A]])
      }
    )
  }

  private def stubCurrentUserRequest(request: FakeRequest[AnyContentAsEmpty.type], isAgent: Boolean): CurrentUserRequest[AnyContentAsEmpty.type] =
    if (isAgent) {
      AuthorisedAndEnrolledAgent[AnyContentAsEmpty.type](SessionData(mtditid = "testMtdItId", nino = testNino, utr = "testUtr"), Some("arn"))(request)
    } else {
      AuthorisedAndEnrolledIndividual[AnyContentAsEmpty.type](mtdItId = "testMtdItId", nino = testNino, navBar = None)(request)
    }

  "complianceTimeLinePage" when {
    "a compliance window is calculated" should {

      "return OK and render the timeline view" when {
        "DES compliance data is returned and the user is mandated" in {
          implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest().withSession("mandation_status" -> "on")
          stubAsMTDUser(isAgent = false, stubCurrentUserRequest(request, isAgent = false))
          val obligationDetails = Seq(
            ObligationDetail(
              status = ComplianceStatusEnum.Open,
              inboundCorrespondenceFromDate = fromDate,
              inboundCorrespondenceToDate = toDate,
              inboundCorrespondenceDateReceived = Some(toDate),
              inboundCorrespondenceDueDate = toDate,
              periodKey = "18A1"
            )
          )
          val optComplianceData = Some(mockComplianceDataWith(obligationDetails))

          when(mockComplianceService.calculateComplianceWindow()(any()))
            .thenReturn(Some((fromDate, toDate)))
          when(mockComplianceService.getDESComplianceData(any(), any(), any())(any()))
            .thenReturn(Future.successful(optComplianceData))
          when(mockTimelineBuilder.buildTimeline(meq(optComplianceData))(any()))
            .thenReturn(Seq.empty)

          val result: Future[Result] = controller().complianceTimelinePage(isAgent = false)(request)
          status(result) shouldBe OK
          verify(mockAuditService, times(1)).audit(any())(any[HeaderCarrier]())
        }

        "DES compliance data is empty and the user is not mandated" in {

          implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest().withSession("mandation_status" -> "off")
          stubAsMTDUser(isAgent = false, stubCurrentUserRequest(request, isAgent = false))

          when(mockComplianceService.calculateComplianceWindow()(any()))
            .thenReturn(Some((fromDate, toDate)))
          when(mockComplianceService.getDESComplianceData(meq(testNino), meq(fromDate), meq(toDate))(any()))
            .thenReturn(Future.successful(None))
          when(mockTimelineBuilder.buildTimeline(meq(None))(any()))
            .thenReturn(Seq.empty)

          val result: Future[Result] = controller().complianceTimelinePage(isAgent = false)(request)
          status(result) shouldBe OK
        }

        "the mandation status session key is absent (defaults to not mandated)" in {

          implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
          stubAsMTDUser(isAgent = false, stubCurrentUserRequest(request, isAgent = false))

          when(mockComplianceService.calculateComplianceWindow()(any()))
            .thenReturn(Some((fromDate, toDate)))
          when(mockComplianceService.getDESComplianceData(meq(testNino), meq(fromDate), meq(toDate))(any()))
            .thenReturn(Future.successful(None))
          when(mockTimelineBuilder.buildTimeline(any())(any()))
            .thenReturn(Seq.empty)
          val result: Future[Result] = controller().complianceTimelinePage(isAgent = false)(request)
          status(result) shouldBe OK
        }

        "audit an empty obligations sequence when no compliance data is returned" in {

          implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
          stubAsMTDUser(isAgent = false, stubCurrentUserRequest(request, isAgent = false))

          when(mockComplianceService.calculateComplianceWindow()(any()))
            .thenReturn(Some((fromDate, toDate)))
          when(mockComplianceService.getDESComplianceData(meq(testNino), meq(fromDate), meq(toDate))(any()))
            .thenReturn(Future.successful(None))
          when(mockTimelineBuilder.buildTimeline(any())(any()))
            .thenReturn(Seq.empty)

          val result: Future[Result] = controller().complianceTimelinePage(isAgent = false)(request)
          status(result) shouldBe OK
          verify(mockAuditService).audit(org.mockito.ArgumentMatchers.argThat[UserComplianceInfoAuditModel] { model =>
            model.complianceData.isEmpty
          })(any[HeaderCarrier]())
        }
        "propagate a failed future if getDESComplianceData fails" in {

          implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
          stubAsMTDUser(isAgent = false, stubCurrentUserRequest(request, isAgent = false))

          when(mockComplianceService.calculateComplianceWindow()(any()))
            .thenReturn(Some((fromDate, toDate)))
          when(mockComplianceService.getDESComplianceData(meq(testNino), meq(fromDate), meq(toDate))(any()))
            .thenReturn(Future.failed(new RuntimeException("DES call failed")))
          val result: Future[Result] = controller().complianceTimelinePage(isAgent = false)(request)
          intercept[RuntimeException] {
            await(result)
          }
          verifyNoInteractions(mockAuditService)
        }

      }
      "no compliance window can be calculated" should {
        "log a warning an return the internal server error page" in {

          implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
          stubAsMTDUser(isAgent = false, stubCurrentUserRequest(request, isAgent = false))

          when(mockComplianceService.calculateComplianceWindow()(any()))
            .thenReturn(None)
          when(mockErrorHandler.showInternalServerError()(any()))
            .thenReturn(Future.successful(InternalServerErrorResult("ISE")))
          val results: Future[Result] = controller().complianceTimelinePage(isAgent = false)(request)
          status(results) shouldBe INTERNAL_SERVER_ERROR
          verify(mockErrorHandler, times(1)).showInternalServerError()(any())
          verify(mockComplianceService, times(1)).calculateComplianceWindow()(any())
          verifyNoInteractions(mockAuditService)
        }
      }
      "it is Agent" should {
        "authenticate via the agent path and still return OK" in {

          implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
          stubAsMTDUser(isAgent = true, stubCurrentUserRequest(request, isAgent = true))

          when(mockComplianceService.calculateComplianceWindow()(any()))
            .thenReturn(Some((fromDate, toDate)))
          when(mockComplianceService.getDESComplianceData(meq(testNino), meq(fromDate), meq(toDate))(any()))
            .thenReturn(Future.successful(None))
          when(mockTimelineBuilder.buildTimeline(any())(any()))
            .thenReturn(Seq.empty)
          val result: Future[Result] = controller().complianceTimelinePage(isAgent = true)(request)
          status(result) shouldBe OK
        }
      }
    }
  }

  private def mockComplianceDataWith(obligations: Seq[ObligationDetail]) = {
    val data = mockitoMock[ComplianceData]
    when(data.obligationDetails)
      .thenReturn(obligations)
    data
  }
}