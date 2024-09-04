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
import play.api.mvc.Results.*
import play.api.mvc.*
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
) (implicit val executionContext: ExecutionContext) extends IdentifierAction with AuthorisedFunctions with Logging {

  override def invokeBlock[A](request: Request[A], block: IdentifierRequest[A] => Future[Result]): Future[Result] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    authorised().retrieve(Retrievals.authorisedEnrolments and Retrievals.nino and Retrievals.affinityGroup) {
      case enrolments ~ Some(nino) ~ Some(Individual) =>
        enrolments.getEnrolment("HMRC-MTD-IT") match {
          case Some(enrolment) =>
            enrolment.getIdentifier("MTDITID") match {
              case Some(id) => block(IdentifierRequest(request, false, nino))
              case None =>
                logger.error("[AuthenticatedIdentifierAction][invokeBlock] MTD IT user without MTDITID")
                successful(InternalServerError)
            }
          case None =>
            logger.error("[AuthenticatedIdentifierAction][invokeBlock] Non-MTD IT user authenticated")
            successful(InternalServerError)
        }
      case _ ~ _ ~ Some(Agent) =>
        request.session.get(SessionKeys.clientMTDID) match {
          case Some(clientMtdItId) =>
            authorised(Enrolment("HMRC-MTD-IT").withIdentifier("MTDITID", clientMtdItId).withDelegatedAuthRule("mtd-it-auth")) {
              // TODO check that the agent is authorised fot this client NINO
              block(IdentifierRequest(request, true, request.session(SessionKeys.clientNino)))
            }
          case None =>
            logger.error("[AuthenticatedIdentifierAction][invokeBlock] Agent with no MTD IT user in session")
            successful(InternalServerError)
        }
      case enrolments ~ oNino ~ oAffinityGroup =>
        logger.error(s"[AuthenticatedIdentifierAction][invokeBlock] ${oAffinityGroup.getOrElse("Unknown")} with NINO=$oNino and ${enrolments.enrolments.size} enrolments")
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
