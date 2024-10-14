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
import play.api.mvc.Results.{Forbidden, InternalServerError, Redirect}
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.retrieve.{EmptyRetrieval, ~}
import uk.gov.hmrc.auth.core.{AffinityGroup, AuthConnector, AuthorisationException, AuthorisedFunctions, Enrolment, Enrolments, FailedRelationship, IncorrectCredentialStrength, InsufficientConfidenceLevel, InternalError, NoActiveSession, SessionRecordNotFound}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import utils.FuturePartition
import utils.FuturePartition.race

import scala.concurrent.Future.successful
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

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
        def redirectToLogin = successful(Redirect(appConfig.loginUrl, Map("continue" -> Seq(appConfig.loginContinueUrl))))

        // start fetching the session data (only needed for agents)
        val sessionFetch: Future[SessionData] = sessionDataConnector.getSessionData

        if (appConfig.featureOptimiseAuthForIndividuals) {
          // start fetching the authentication result for an individual
          val individualFetch: Future[(Boolean, String, String)] = authConnector.authorise(Enrolment("HMRC-MTD-IT"), Retrievals.authorisedEnrolments and Retrievals.nino).map {
            case ~(enrolments: Enrolments, Some(nino)) => (enrolments.getEnrolment("HMRC-MTD-IT") map { (id: Enrolment) => (false, id.getIdentifier("MTDITID").get.value, nino)}).get
            case _ => throw InternalError()
          }

          def individualCut = individualFetch.isCompleted && individualFetch.value.exists(_.isSuccess)

          // additionally, start fetching the authentication result for an agent,
          // unless the individual result is already available and indicates success
          val agentFetch: Future[(Boolean, String, String)] = sessionFetch flatMap {
            case sessionData if sessionData.mtditid.isDefined && sessionData.nino.isDefined && !individualCut =>
              authConnector.authorise(Enrolment("HMRC-MTD-IT").withIdentifier("MTDITID", sessionData.mtditid.get).withDelegatedAuthRule("mtd-it-auth"), EmptyRetrieval).map {
                _ => (true, sessionData.mtditid.get, sessionData.nino.get)
              }
            case _ => throw SessionRecordNotFound()
          }

          // only one authenication type can apply, so whichever works first is our winner
          val partitioned = race(individualFetch, agentFetch)
          partitioned.map(_.head).transformWith {
            case Success(Success(result)) => successful((Some(Success(result)),None))
            case Success(Failure(failure)) => partitioned.flatMap(_.next).map(_.head).transform(v=>Success((Some(Failure(failure)),v.toOption)))
            case _ => throw InternalError()
          }.map {
            case (Some(Success((isAgent: Boolean, _: String, nino: String))),_) => block(IdentifierRequest(request, isAgent, nino))
            case (_, Some(Success((isAgent: Boolean, _: String, nino: String)))) => block(IdentifierRequest(request, isAgent, nino))
            case (Some(Failure(_: SessionRecordNotFound)),Some(Failure(exc))) => throw exc
            case (Some(Failure(exc)),_) => throw exc
            case _ => throw InternalError()
          }.recover {
            case _: NoActiveSession => redirectToLogin
            case _: InsufficientConfidenceLevel | _: IncorrectCredentialStrength | _: FailedRelationship => successful(Forbidden)
            case _: AuthorisationException => successful(InternalServerError)
          }.flatten
        } else {
          sessionFetch.flatMap {
            case SessionData(Some(mtditid), Some(nino), _, Some(_)) =>
              authConnector.authorise(Enrolment("HMRC-MTD-IT") or Enrolment("HMRC-MTD-IT").withIdentifier("MTDITID", mtditid).withDelegatedAuthRule("mtd-it-auth"), Retrievals.affinityGroup).flatMap {
                case Some(AffinityGroup.Individual) => block(IdentifierRequest(request, false, nino))
                case Some(AffinityGroup.Agent) => block(IdentifierRequest(request, true, nino))
                case _ => throw InternalError()
              }
            case _ => redirectToLogin
          }
        }
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