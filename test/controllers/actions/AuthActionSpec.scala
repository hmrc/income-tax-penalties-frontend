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
import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Level.ERROR
import com.google.inject.Inject
import config.AppConfig
import controllers.routes
import play.api.Logger
import play.api.mvc.{Action, AnyContent, BodyParsers, Results}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.auth.core.*
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.Retrieval
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class AuthActionSpec extends SpecBase {

  class Harness(authAction: IdentifierAction) {
    def onPageLoad(): Action[AnyContent] = authAction { request =>
      Results.Ok(s"${request.isAgent} - ${request.clientNino}")
    }
  }

  lazy val targetLogger = Logger(classOf[AuthenticatedIdentifierAction])

  "Auth Action" - {
    "when the user hasn't logged in" - {
      "must redirect the user to log in " in {
        val application = applicationBuilder().build()

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig   = application.injector.instanceOf[AppConfig]

          val authAction = new AuthenticatedIdentifierAction(new FakeFailingAuthConnector(new MissingBearerToken), appConfig, bodyParsers)
          val controller = new Harness(authAction)
          val result = controller.onPageLoad()(FakeRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value must startWith(appConfig.loginUrl)
        }
      }
    }

    "the user's session has expired" - {
      "must redirect the user to log in " in {
        val application = applicationBuilder().build()

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig   = application.injector.instanceOf[AppConfig]

          val authAction = new AuthenticatedIdentifierAction(new FakeFailingAuthConnector(new BearerTokenExpired), appConfig, bodyParsers)
          val controller = new Harness(authAction)
          val result = controller.onPageLoad()(FakeRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value must startWith(appConfig.loginUrl)
        }
      }
    }

    "the user doesn't have sufficient enrolments" - {
      "must redirect the user to the unauthorised page" in {
        val application = applicationBuilder().build()

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig   = application.injector.instanceOf[AppConfig]

          val authAction = new AuthenticatedIdentifierAction(new FakeFailingAuthConnector(new InsufficientEnrolments), appConfig, bodyParsers)
          val controller = new Harness(authAction)
          val result = controller.onPageLoad()(FakeRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe routes.UnauthorisedController.onPageLoad().url
        }
      }
    }

    "the user doesn't have sufficient confidence level" - {
      "must redirect the user to the unauthorised page" in {
        val application = applicationBuilder().build()

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig   = application.injector.instanceOf[AppConfig]

          val authAction = new AuthenticatedIdentifierAction(new FakeFailingAuthConnector(new InsufficientConfidenceLevel), appConfig, bodyParsers)
          val controller = new Harness(authAction)
          val result = controller.onPageLoad()(FakeRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe routes.UnauthorisedController.onPageLoad().url
        }
      }
    }

    "the user used an unaccepted auth provider" - {
      "must redirect the user to the unauthorised page" in {
        val application = applicationBuilder().build()

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig   = application.injector.instanceOf[AppConfig]

          val authAction = new AuthenticatedIdentifierAction(new FakeFailingAuthConnector(new UnsupportedAuthProvider), appConfig, bodyParsers)
          val controller = new Harness(authAction)
          val result = controller.onPageLoad()(FakeRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe routes.UnauthorisedController.onPageLoad().url
        }
      }
    }

    "the user has an unsupported affinity group" - {
      "must redirect the user to the unauthorised page" in {
        val application = applicationBuilder().build()

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig   = application.injector.instanceOf[AppConfig]

          val authAction = new AuthenticatedIdentifierAction(new FakeFailingAuthConnector(new UnsupportedAffinityGroup), appConfig, bodyParsers)
          val controller = new Harness(authAction)
          val result = controller.onPageLoad()(FakeRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(routes.UnauthorisedController.onPageLoad().url)
        }
      }
    }

    "the user has an unsupported credential role" - {
      "must redirect the user to the unauthorised page" in {
        val application = applicationBuilder().build()

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig   = application.injector.instanceOf[AppConfig]

          val authAction = new AuthenticatedIdentifierAction(new FakeFailingAuthConnector(new UnsupportedCredentialRole), appConfig, bodyParsers)
          val controller = new Harness(authAction)
          val result = controller.onPageLoad()(FakeRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(routes.UnauthorisedController.onPageLoad().url)
        }
      }
    }

    "the user has MTDITID (Making Tax Digital Income Tax ID) and a NINO (National Insurance Number)" - {
      "should work" in {
        val application = applicationBuilder().build()

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig   = application.injector.instanceOf[AppConfig]

          val authAction = new AuthenticatedIdentifierAction(new FakeSuccessfulAuthConnector(), appConfig, bodyParsers)
          val controller = new Harness(authAction)
          val result = controller.onPageLoad()(FakeRequest())

          status(result) mustBe OK
          contentAsString(result) mustBe "false - bar"
        }
      }
    }

    "the user has MTDITID (Making Tax Digital Income Tax ID) but no NINO (National Insurance Number)" - {
      "should fail with internal server error" in {
        val application = applicationBuilder().build()

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig   = application.injector.instanceOf[AppConfig]

          val authAction = new AuthenticatedIdentifierAction(new FakeSuccessfulAuthConnector(nino = None), appConfig, bodyParsers)
          val controller = new Harness(authAction)

          withCaptureOfLoggingFrom(targetLogger) { log =>
            val result = controller.onPageLoad()(FakeRequest())

            status(result) mustBe INTERNAL_SERVER_ERROR

            log.messages.filter(_._1.isGreaterOrEqual(Level.INFO)) mustBe List(
              ERROR -> "[AuthenticatedIdentifierAction][invokeBlock] MTD IT user without NINO"
            )
          }
        }
      }
    }

    "the user has MTD (Making Tax Digital) but no MTDITID (Making Tax Digital Income Tax ID)" - {
      "should fail with internal server error" in {
        val application = applicationBuilder().build()

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig   = application.injector.instanceOf[AppConfig]

          val authAction = new AuthenticatedIdentifierAction(new FakeSuccessfulAuthConnector(mtdItId = None), appConfig, bodyParsers)
          val controller = new Harness(authAction)

          withCaptureOfLoggingFrom(targetLogger) { log =>
            val result = controller.onPageLoad()(FakeRequest())

            status(result) mustBe INTERNAL_SERVER_ERROR

            log.messages.filter(_._1.isGreaterOrEqual(Level.INFO)) mustBe List(
              ERROR -> "[AuthenticatedIdentifierAction][invokeBlock] MTD IT user without MTDITID"
            )
          }
        }
      }
    }

    "the user authenticates despite not being enrolled for MTD (Making Tax Digital)" - {
      "should return internal error and log an error" in {
        val application = applicationBuilder().build()

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig   = application.injector.instanceOf[AppConfig]

          val authAction = new AuthenticatedIdentifierAction(new FakeNonMTDAuthConnector(), appConfig, bodyParsers)
          val controller = new Harness(authAction)

          withCaptureOfLoggingFrom(targetLogger) { log =>
            val result = controller.onPageLoad()(FakeRequest())

            status(result) mustBe INTERNAL_SERVER_ERROR

            log.messages.filter(_._1.isGreaterOrEqual(Level.INFO)) mustBe List(
              ERROR -> "[AuthenticatedIdentifierAction][invokeBlock] Non-MTD IT user authenticated"
            )
          }
        }
      }
    }
  }
}

class FakeFailingAuthConnector @Inject()(exceptionToReturn: Throwable) extends AuthConnector {
  override def authorise[A](predicate: Predicate, retrieval: Retrieval[A])(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[A] =
    Future.failed(exceptionToReturn)
}

import uk.gov.hmrc.auth.core.retrieve._

class FakeNonMTDAuthConnector @Inject()(nino: Option[String] = Some("bar")) extends AuthConnector {
  override def authorise[A](predicate: Predicate, retrieval: Retrieval[A])(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[A] = {
    val x = new ~(Enrolments(Set()), nino)
    Future.successful( x.asInstanceOf[A] )
  }
}

class FakeSuccessfulAuthConnector @Inject()(mtdItId: Option[String] = Some("foo"), nino: Option[String] = Some("bar")) extends AuthConnector {
  override def authorise[A](predicate: Predicate, retrieval: Retrieval[A])(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[A] = {
    val enrolment = mtdItId match {
      case Some(id) => Set(Enrolment("HMRC-MTD-IT").withIdentifier("MTDITID", id))
      case None => Set(Enrolment("HMRC-MTD-IT"))
    }
    val x = new ~(Enrolments(enrolment), nino)
    Future.successful( x.asInstanceOf[A] )
  }
}
