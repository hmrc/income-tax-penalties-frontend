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
import play.api.mvc.Results.{InternalServerError, Redirect}
import uk.gov.hmrc.auth.core.*
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import utils.ExceptionUtils.given

import scala.concurrent.Future.successful
import scala.concurrent.{ExecutionContext, Future}

trait AgentAction {
  def apply(clientNino: String): ActionBuilder[IdentifierRequest, AnyContent]
}

class AuthenticatedAgentAction @Inject()(override val authConnector: AuthConnector, config: AppConfig, val defaultParser: BodyParsers.Default)
  (implicit val executionContext: ExecutionContext) 
  extends AgentAction with AuthorisedFunctions with Logging {

  class Impl(clientMTDITID: String, val parser: BodyParsers.Default)(implicit val executionContext: ExecutionContext) extends IdentifierAction {
    override def invokeBlock[A](request: Request[A], block: IdentifierRequest[A] => Future[Result]): Future[Result] = {
      implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
      authorised(Enrolment("HMRC-MTD-IT").withIdentifier("MTDITID", clientMTDITID).withDelegatedAuthRule("mtd-it-auth")) {
        val sessionClientMTDITID = request.session(SessionKeys.clientMTDID)
        if (sessionClientMTDITID == clientMTDITID) {
          val sessionClientNINO = request.session(SessionKeys.clientNino)
          logger.warn(s"[AuthenticatedIdentifierAction][invokeBlock] Using unchecked client NINO from session ($sessionClientNINO)")
          block(IdentifierRequest(request, true, sessionClientNINO))
        } else {
          logger.error(s"[AuthenticatedIdentifierAction][invokeBlock] Session client MTDITID ($sessionClientMTDITID) does not match request ($clientMTDITID)")
          successful(InternalServerError)
        }
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
  
  def apply(clientNino: String): ActionBuilder[IdentifierRequest, AnyContent] = new Impl(clientNino, defaultParser)
  
}
