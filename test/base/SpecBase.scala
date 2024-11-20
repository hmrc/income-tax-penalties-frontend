/*
 * Copyright 2023 HM Revenue & Customs
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

package base

import base.testData.{ComplianceDataTestData, PenaltiesDetailsTestData}
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.User
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.Mockito.mock
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.Injector
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.api.test.FakeRequest
import play.twirl.api.Html
import uk.gov.hmrc.incometaxpenaltiesfrontend.services.AuthService
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.incometaxpenaltiesfrontend.utils.SessionKeys
import uk.gov.hmrc.incometaxpenaltiesfrontend.viewmodels.{SummaryCardHelper, TimelineHelper}
import uk.gov.hmrc.incometaxpenaltiesfrontend.views.html.templates.Unauthorised
import uk.gov.hmrc.incometaxpenaltiesfrontend.config.{AppConfig, ErrorHandler}
import uk.gov.hmrc.incometaxpenaltiesfrontend.controllers.predicates.AuthPredicate

import java.time.temporal.ChronoUnit
import java.time.{LocalDate, LocalDateTime}
import scala.concurrent.ExecutionContext.Implicits.global

trait SpecBase extends AnyWordSpec with Matchers with GuiceOneAppPerSuite with PenaltiesDetailsTestData with ComplianceDataTestData {
  implicit val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  implicit val hc: HeaderCarrier = HeaderCarrier()

  lazy val injector: Injector = app.injector

  val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]

  implicit val fakeRequest: FakeRequest[AnyContent] = FakeRequest("GET", "/")

  implicit val messages: Messages = messagesApi.preferred(fakeRequest)

  val cyMessages: Messages = messagesApi.preferred(fakeRequest.withTransientLang("cy"))

  val mcc: MessagesControllerComponents = injector.instanceOf[MessagesControllerComponents]

  lazy val errorHandler: ErrorHandler = injector.instanceOf[ErrorHandler]

  val unauthorised: Unauthorised = injector.instanceOf[Unauthorised]

  val mockAuthConnector: AuthConnector = mock(classOf[AuthConnector])

  val mockAuthService: AuthService = new AuthService(mockAuthConnector)

  val summaryCardHelper: SummaryCardHelper = injector.instanceOf[SummaryCardHelper]

  val timelineHelper: TimelineHelper = injector.instanceOf[TimelineHelper]

  val vrn: String = "123456789"

  val sampleDateTime: LocalDateTime = LocalDateTime.of(2021, 4, 23, 18, 25, 43)
    .plus(511, ChronoUnit.MILLIS)
  val sampleDateV2: LocalDate = LocalDate.of(2021, 4, 23)
  val sampleOldestDate: LocalDate = LocalDate.of(2021, 1, 1)

  lazy val authPredicate: AuthPredicate = new AuthPredicate(
    messagesApi,
    mcc,
    mockAuthService,
    errorHandler,
    unauthorised
  )

  val quarterlyThreshold: Int = 4

  val annualThreshold: Int = 2

  val monthlyThreshold: Int = 5

  def asDocument(html: Html): Document = Jsoup.parse(html.toString())

  val penaltyId = "123456789"

  val redirectToAppealUrlForLSP: String =
    uk.gov.hmrc.incometaxpenaltiesfrontend.controllers.routes.IndexController.redirectToAppeals(penaltyId, isLPP = false, isFindOutHowToAppealLSP = false, isAdditional = false).url

  val redirectToAppealUrlForLPP: String =
    uk.gov.hmrc.incometaxpenaltiesfrontend.controllers.routes.IndexController.redirectToAppeals(penaltyId, isLPP = true, isFindOutHowToAppealLSP = false, isAdditional = false).url

  val redirectToAppealObligationUrlForLSP: String =
    uk.gov.hmrc.incometaxpenaltiesfrontend.controllers.routes.IndexController.redirectToAppeals(penaltyId, isLPP = false, isFindOutHowToAppealLSP = true, isAdditional = false).url

  val redirectToAppealObligationUrlForLPP: String =
    uk.gov.hmrc.incometaxpenaltiesfrontend.controllers.routes.IndexController.redirectToAppeals(penaltyId, isLPP = true, isFindOutHowToAppealLSP = true, isAdditional = false).url

  val redirectToAppealObligationUrlForLPPAdditional: String =
    uk.gov.hmrc.incometaxpenaltiesfrontend.controllers.routes.IndexController.redirectToAppeals(penaltyId, isLPP = true, isFindOutHowToAppealLSP = false, isAdditional = true).url

  val vatTraderUser: User[AnyContent] = User("123456789", arn = None)(fakeRequest)

  val agentUser: User[AnyContent] = User("123456789", arn = Some("AGENT1"))(fakeRequest.withSession(SessionKeys.agentSessionVrn -> "VRN1234"))
}
