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

package controllers.actions

import base.SpecBase
import com.google.inject.Inject
import config.AppConfig
import connectors.SessionDataConnector
import connectors.SessionDataConnector.SessionData
import controllers.agent.SessionKeys
import controllers.agent.SessionKeys.clientMTDID
import models.requests.IdentifierRequest
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.mockito.{ArgumentMatchers, Mockito}
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.mvc.*
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.auth.core.*
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future.successful
import scala.concurrent.{ExecutionContext, Future}

class CombinedActionSpec extends SpecBase with TableDrivenPropertyChecks {

  "When new session feature is switched off "- {
    class Setup(useSessionService: Boolean) {
      val authConnector = mock[AuthConnector]
      val appConfig = mock[AppConfig]
      val sessionDataConnector = mock[SessionDataConnector]
      val agentAction = mock[AgentAction]
      val individualAction = mock[IdentifierAction]
      val bodyParser = mock[BodyParsers.Default]

      when(appConfig.featureUseSessionService).thenReturn(useSessionService)

      val combinedActionBuilder: ActionBuilder[IdentifierRequest, AnyContent] = new CombinedAction(authConnector, appConfig, sessionDataConnector, individualAction, agentAction, bodyParser).apply()
      val combinedAction = combinedActionBuilder { (_: IdentifierRequest[AnyContent]) => Results.NoContent }
    }

    "delegates individuals to the individual auth action" in new Setup(useSessionService = false) {
        combinedAction(FakeRequest())

        verify(individualAction).invokeBlock(any(), any())
        verifyNoMoreInteractions(agentAction, individualAction)
    }

    "delegates agents to the agent auth action" in new Setup(useSessionService = false) {
      val actionBuilder = mock[ActionBuilder[_,_]]
      when(agentAction.apply("foo")).thenReturn(actionBuilder)

      combinedAction(FakeRequest().withSession(clientMTDID -> "foo"))

      verify(agentAction).apply("foo")
      verify(actionBuilder).invokeBlock(any(), any())
      verifyNoMoreInteractions(agentAction, individualAction, actionBuilder)
    }
  }

  "When new session feature is switched on " - {
    class Harness(authAction: CombinedAction) {
      def onPageLoad(): Action[AnyContent] = authAction() { request =>
        Results.Ok(s"${request.isAgent} - ${request.clientNino}")
      }
    }

    val noSessionDataConnector = new SessionDataConnector(null,null) {
      override def getSessionData(using headerCarrier: HeaderCarrier): Future[SessionData] = successful(SessionData(sessionId = None))
    }
    val okSessionDataConnector = new SessionDataConnector(null,null) {
      override def getSessionData(using headerCarrier: HeaderCarrier): Future[SessionData] = successful(SessionData(mtditid = Some("foo"), nino = Some("bar"), sessionId = Some("123")))
    }

    val applicationOptimizedForIndividuals = applicationBuilder().configure("feature.useSessionService" -> true).build()
    val applicationWithBalancedAuthentication = applicationBuilder().configure("feature.useSessionService" -> true, "feature.optimiseAuthForIndividuals" -> false).build()

    "must redirect the user to log in" - {
      Table(
        ("Problem", "Auth Exception"),
        ("the user is not logged in ", new MissingBearerToken),
        ("the user's session has expired", new BearerTokenExpired),
        ("the user's session is invalid", new InvalidBearerToken),
        ("the user's session does not exist", new SessionRecordNotFound)
      ).forEvery { (problem, authException) =>
        s"when $problem " in {
          val application = applicationOptimizedForIndividuals
          running(application) {
            val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
            val appConfig = application.injector.instanceOf[AppConfig]

            val authAction = new CombinedAction(new FakeFailingAuthConnector(authException), appConfig, noSessionDataConnector, null, null, bodyParsers)
            val controller = new Harness(authAction)
            val result = controller.onPageLoad()(FakeRequest())

            status(result) mustBe SEE_OTHER
            redirectLocation(result).value must startWith(appConfig.loginUrl)
          }
        }
      }
    }

    "must deny access" - {
      Table(
        ("Problem", "Auth Exception"),
        ("auth lacks confidence in this user's identity", new InsufficientConfidenceLevel),
        ("the user's credentials are too weak", new IncorrectCredentialStrength),
        ("the agent-client relationship is not estanblished", new FailedRelationship)
      ).forEvery { (problem, authException) =>
        s"when $problem " in {
          val application = applicationOptimizedForIndividuals
          running(application) {
            val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
            val appConfig = application.injector.instanceOf[AppConfig]

            val authAction = new CombinedAction(new FakeFailingAuthConnector(authException), appConfig, noSessionDataConnector, null, null, bodyParsers)
            val controller = new Harness(authAction)
            val result = controller.onPageLoad()(FakeRequest())

            status(result) mustBe FORBIDDEN
          }
        }
      }
    }

    "return internal server error" - {
      Table(
        ("Problem", "Auth Exception"),
        ("the user is of an unsupported type", new UnsupportedAffinityGroup),
        ("the user lacks necessary enrolments", new InsufficientEnrolments),
        ("the user has an unsupported credential role", new UnsupportedCredentialRole),
        ("we do not support this auth provider", new UnsupportedAuthProvider),
        ("there is an internal error in auth", new InternalError),
        ("the user's nino is incorrect", IncorrectNino)
      ).forEvery { (problem, authException) =>
        s"when $problem " in {
          val application = applicationOptimizedForIndividuals
          running(application) {
            val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
            val appConfig = application.injector.instanceOf[AppConfig]

            val authAction = new CombinedAction(new FakeFailingAuthConnector(authException), appConfig, noSessionDataConnector, null, null, bodyParsers)
            val controller = new Harness(authAction)
            val result = controller.onPageLoad()(FakeRequest())

            status(result) mustBe INTERNAL_SERVER_ERROR
          }
        }
      }
    }

    "Individual Action" - {
      "the user has MTDITID (Making Tax Digital Income Tax ID) and a NINO (National Insurance Number)" - {
        "should work when optimized for individuals" in {
          val application = applicationOptimizedForIndividuals
          running(application) {
            val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
            val appConfig = application.injector.instanceOf[AppConfig]

            val authAction = new CombinedAction(new FakeSuccessfulAuthConnector(), appConfig, noSessionDataConnector, null, null, bodyParsers)
            val controller = new Harness(authAction)
            val result = controller.onPageLoad()(FakeRequest())

            status(result) mustBe OK
            contentAsString(result) mustBe "false - bar"
          }
        }

        "should work with balanced optimisation" in {
          val application = applicationWithBalancedAuthentication
          running(application) {
            val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
            val appConfig = application.injector.instanceOf[AppConfig]

            val authAction = CombinedAction(new FakeSuccessfulBalancedAuthConnector(isAgent = false), appConfig, okSessionDataConnector, null, null, bodyParsers)
            val controller = new Harness(authAction)
            val result = controller.onPageLoad()(FakeRequest())

            status(result) mustBe OK
            contentAsString(result) mustBe "false - bar"
          }
        }
      }
    }

    "Agent Action" - {
      "session service has MTDITID (Making Tax Digital Income Tax ID) and a NINO (National Insurance Number)" - {
        "should work when optimized for individuals" in {
          val application = applicationOptimizedForIndividuals
          running(application) {
            val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
            val appConfig = application.injector.instanceOf[AppConfig]

            val authAction = CombinedAction(new FakeSuccessfulCombinedAuthConnector(isAgent = true), appConfig, okSessionDataConnector, null, null, bodyParsers)
            val controller = new Harness(authAction)
            val result = controller.onPageLoad()(FakeRequest())

            status(result) mustBe OK
            contentAsString(result) mustBe "true - bar"
          }
        }

        "should work with balanced optimisation" in {
          val application = applicationWithBalancedAuthentication
          running(application) {
            val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
            val appConfig = application.injector.instanceOf[AppConfig]

            val authAction = CombinedAction(new FakeSuccessfulBalancedAuthConnector(isAgent = true), appConfig, okSessionDataConnector, null, null, bodyParsers)
            val controller = new Harness(authAction)
            val result = controller.onPageLoad()(FakeRequest())

            status(result) mustBe OK
            contentAsString(result) mustBe "true - bar"
          }
        }
      }
    }
  }
}

class FakeSuccessfulBalancedAuthConnector @Inject()(isAgent: Boolean) extends AuthConnector {
  override def authorise[A](predicate: Predicate, retrieval: Retrieval[A])(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[A] = {
    val affinityGroup: AffinityGroup = if (isAgent) AffinityGroup.Agent else AffinityGroup.Individual
    Future.successful(Some(affinityGroup).asInstanceOf[A])
  }
}

class FakeSuccessfulCombinedAuthConnector @Inject()(isAgent: Boolean) extends AuthConnector {
  override def authorise[A](predicate: Predicate, retrieval: Retrieval[A])(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[A] = {
    (predicate, isAgent) match {
      case (enr: Enrolment, true) if enr.identifiers.isEmpty =>
        Future.failed(InsufficientEnrolments())
      case (enr: Enrolment, false) if enr.identifiers.isEmpty =>
        val enrolment = Set(Enrolment("HMRC-MTD-IT").withIdentifier("MTDITID", "foo"))
        val retreivals = new~(Enrolments(enrolment), Some("bar"))
        Future.successful(retreivals.asInstanceOf[A])
      case _ =>
        Future.successful( ().asInstanceOf[A] )
    }
  }
}


