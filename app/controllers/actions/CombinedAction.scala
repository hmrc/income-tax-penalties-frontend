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
import connectors.SessionDataConnector
import connectors.SessionDataConnector.SessionData
import controllers.agent.SessionKeys
import models.requests.IdentifierRequest
import play.api.mvc.*
import play.api.mvc.Results.{NotFound, Redirect}
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.retrieve.{EmptyRetrieval, ~}
import uk.gov.hmrc.auth.core.{AffinityGroup, AuthConnector, AuthorisedFunctions, Enrolment}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import utils.FuturePartition.race
import utils.EnrolmentKeys

import scala.concurrent.Future.successful
import scala.concurrent.{ExecutionContext, Future}

class CombinedAction @Inject()(
  override val authConnector: AuthConnector,
  appConfig: AppConfig,
  sessionDataConnector: SessionDataConnector,
  identifierAction: IdentifierAction,
  agentAction: AgentAction,
  defaultParser: BodyParsers.Default
)(implicit val executionContext: ExecutionContext) extends AuthorisedFunctions {
  class Impl(val parser: BodyParsers.Default)(implicit val executionContext: ExecutionContext) extends IdentifierAction {
    override def invokeBlock[A](request: Request[A], block: IdentifierRequest[A] => Future[Result]): Future[Result] = {
      if (appConfig.featureUseSessionService) {
        given HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

        // start fetching the session data (only needed for agents)
        val sessionFetch: Future[SessionData] = sessionDataConnector.getSessionData

        val mainStage = if (appConfig.featureOptimiseAuthForIndividuals) {
          // start fetching the authentication result for an individual
          val individualFetch: Future[Option[(Boolean, String, String)]] = authConnector.authorise(Enrolment(EnrolmentKeys.mtdEnrolmentKey), Retrievals.authorisedEnrolments and Retrievals.nino).map {
            case ~(enrolments, Some(nino)) => enrolments.getEnrolment(EnrolmentKeys.mtdEnrolmentKey) map (id => (false, id.getIdentifier(EnrolmentKeys.mtdId).get.value, nino))
          }.recover(_ => None)

          def individualCut = individualFetch.isCompleted && individualFetch.value.exists(_.toOption.flatten.isDefined)

          // additionally, start fetching the authentication result for an agent,
          // unless the individual result is already available and indicates success
          val agentFetch: Future[Option[(Boolean, String, String)]] = sessionFetch flatMap {
            case sessionData if sessionData.mtditid.isDefined && sessionData.nino.isDefined && !individualCut =>
              authConnector.authorise(Enrolment(EnrolmentKeys.mtdEnrolmentKey).withIdentifier(EnrolmentKeys.mtdId, sessionData.mtditid.get).withDelegatedAuthRule(EnrolmentKeys.agentDelegatedAuthRuleKey), EmptyRetrieval).map {
                _ => Some((true, sessionData.mtditid.get, sessionData.nino.get))
              }.recover(_ => None)
            case _ => successful(None)
          }

          // only one authenication type can apply, so whichever works first is our winner
          race(individualFetch, agentFetch).firstNonEmpty.flatMap {
            case (isAgent: Boolean, _: String, nino: String) => block(IdentifierRequest(request, isAgent, nino))
          }
        } else {
          sessionFetch.flatMap {
            case SessionData(Some(mtditid), Some(nino), _, Some(_)) =>
              authConnector.authorise(Enrolment(EnrolmentKeys.mtdEnrolmentKey) or Enrolment(EnrolmentKeys.mtdEnrolmentKey).withIdentifier(EnrolmentKeys.mtdId, mtditid).withDelegatedAuthRule(EnrolmentKeys.agentDelegatedAuthRuleKey), Retrievals.affinityGroup).flatMap {
                case Some(AffinityGroup.Individual) => block(IdentifierRequest(request, false, nino))
                case Some(AffinityGroup.Agent) => block(IdentifierRequest(request, true, nino))
              }
          }
        }

        mainStage.recover(_=>Redirect(appConfig.loginUrl, Map("continue" -> Seq(appConfig.loginContinueUrl ))))
      } else {
        request.session.get(SessionKeys.clientMTDID) match {
          case Some(mtdIdId) => agentAction(mtdIdId).invokeBlock(request, block)
          case None => identifierAction.invokeBlock(request, block)
        }
      }
    }
  }

  def apply(): ActionBuilder[IdentifierRequest, AnyContent] = new Impl(defaultParser)
}