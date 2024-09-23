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
import connectors.SessionDataConnector
import controllers.agent.SessionKeys
import models.requests.IdentifierRequest
import play.api.Logging
import play.api.mvc.*
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.{AuthConnector, AuthorisedFunctions}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import scala.concurrent.{ExecutionContext, Future}

class CombinedAction @Inject()(
    identifierAction: IdentifierAction,
    agentAction: AgentAction,
    defaultParser: BodyParsers.Default,
    sessionDataConnector: SessionDataConnector
  )(implicit val executionContext: ExecutionContext) {
  class Impl(val parser: BodyParsers.Default)(implicit val executionContext: ExecutionContext) extends IdentifierAction {
    override def invokeBlock[A](request: Request[A], block: IdentifierRequest[A] => Future[Result]): Future[Result] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
      for (sessionData <- sessionDataConnector.getSessionData) yield {
        (sessionData.mtditid, sessionData.nino) match {
          case (Some(mtditid), Some(nino)) => agentAction(mtditid, nino).invokeBlock(request, block)
          case _ => identifierAction.invokeBlock(request, block)
        }
      }
    }.flatten
  }

  def apply(): ActionBuilder[IdentifierRequest, AnyContent] = new Impl(defaultParser)
}
