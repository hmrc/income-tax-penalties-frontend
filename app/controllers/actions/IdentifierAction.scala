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

import com.google.inject.Inject
import config.AppConfig
import controllers.agent.SessionKeys
import controllers.routes
import models.requests.IdentifierRequest
import play.api.Logging
import play.api.mvc.*
import play.api.mvc.Results.*
import uk.gov.hmrc.auth.core.*
import uk.gov.hmrc.auth.core.AffinityGroup.{Agent, Individual}
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import utils.ExceptionUtils.ThrowableImplicits

import scala.concurrent.Future.successful
import scala.concurrent.{ExecutionContext, Future}

trait IdentifierAction extends ActionBuilder[IdentifierRequest, AnyContent] with ActionFunction[Request, IdentifierRequest]

class AuthenticatedIdentifierAction @Inject()(
                                               override val authConnector: AuthConnector,
                                               config: AppConfig,
                                               val parser: BodyParsers.Default
                                             )(implicit val executionContext: ExecutionContext) extends IdentifierAction with AuthorisedFunctions with Logging {

  override def invokeBlock[A](request: Request[A], block: IdentifierRequest[A] => Future[Result]): Future[Result] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    authorised(Enrolment("HMRC-MTD-IT").withDelegatedAuthRule("mtd-it-auth")).retrieve(Retrievals.nino and Retrievals.affinityGroup) {
      case Some(nino) ~ Some(Individual) => block(IdentifierRequest(request, false, nino))
      case Some(nino) ~ Some(Agent) => block(IdentifierRequest(request, true, nino))
      case _ ~ Some(other) =>
        logger.error(s"[AuthenticatedIdentifierAction][invokeBlock] MTD IT $other not supported")
        successful(InternalServerError)
      case ~(None, Some(Agent)) =>
        request.session.get(SessionKeys.clientNino) match {
          case Some(clientNino) =>
            authorised(Enrolment("HMRC-AS-AGENT").withIdentifier("NINO", clientNino).withDelegatedAuthRule("mtd-it-auth")).retrieve(Retrievals.allEnrolments) {
              enrolments =>
                enrolments.enrolments.collectFirst {
                  case Enrolment("HMRC-AS-AGENT", Seq(EnrolmentIdentifier(_, arn)), "Activated", _) => arn
                } match {
                  case Some(arn) => block(IdentifierRequest(request = request, isAgent = true, clientNino = clientNino))
                }
            }
        }
      case ~(Some(_), _) =>
        logger.error("[AuthenticatedIdentifierAction][invokeBlock] MTD IT user without NINO")
        successful(InternalServerError)
      case ~(Some(_), None) =>
        logger.error("[AuthenticatedIdentifierAction][invokeBlock] MTD IT user without NINO")
        successful(InternalServerError)
    } recover {
      case _: NoActiveSession =>
        Redirect(config.loginUrl, Map("continue" -> Seq(config.loginContinueUrl)))
      case _: InsufficientEnrolments |
           _: InsufficientConfidenceLevel |
           _: UnsupportedAuthProvider |
           _: UnsupportedAffinityGroup |
           _: UnsupportedCredentialRole =>
        Redirect(routes.UnauthorisedController.onPageLoad())
      case e =>
        logger.error(s"[AuthenticatedIdentifierAction][invokeBlock] ${e.summary}", e)
        InternalServerError
    }
  }
}
