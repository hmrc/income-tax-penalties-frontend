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

package uk.gov.hmrc.incometaxpenaltiesfrontend.controllers.predicates

import play.api.Logging
import play.api.i18n.Messages
import play.api.mvc.Results.{Forbidden, NotImplemented, Redirect}
import play.api.mvc._
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.allEnrolments
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}
import uk.gov.hmrc.incometaxpenaltiesfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.User
import uk.gov.hmrc.incometaxpenaltiesfrontend.utils.{EnrolmentKeys, SessionKeys}
import uk.gov.hmrc.incometaxpenaltiesfrontend.views.html.templates.Unauthorised
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AuthPredicate @Inject()(val authConnector: AuthConnector,
                              unauthorisedView: Unauthorised,
                              mcc: MessagesControllerComponents
                             )(implicit appConfig: AppConfig,
                               implicit val executionContext: ExecutionContext)
  extends AuthorisedFunctions with ActionBuilder[User, AnyContent] with ActionFunction[Request, User] with Logging {

  override def parser: BodyParser[AnyContent] = mcc.parsers.defaultBodyParser

  override def invokeBlock[A](request: Request[A], block: User[A] => Future[Result]): Future[Result] = {
    implicit val req: Request[A] = request
    implicit val messages: Messages = mcc.messagesApi.preferred(request)
    implicit val headerCarrier: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    val logMsgStart: String = "[AuthPredicate][invokeBlock]"
    authorised().retrieve(Retrievals.affinityGroup and Retrievals.allEnrolments) {
      case Some(affinityGroup) ~ allEnrolments =>
        logger.debug(s"$logMsgStart - User is $affinityGroup and has ${allEnrolments.enrolments.size} enrolments. " +
          s"Enrolments: ${allEnrolments.enrolments}")
        (isAgent(affinityGroup), allEnrolments) match {
          case (true, _) =>
            logger.debug(s"$logMsgStart - Authorising user as Agent")
            authoriseAsAgent(block)
          case (false, enrolments) =>
            logger.debug(s"$logMsgStart - Authorising user as Individual/Organisation")
            checkVatEnrolment(enrolments, block)
        }
      case _ =>
        logger.warn(s"$logMsgStart - Missing affinity group")
        throw new InternalServerException("Missing affinity group")
    } recover {
      case _: NoActiveSession =>
        logger.debug(s"$logMsgStart - No active session, redirect to GG sign in")
        Redirect(appConfig.signInUrl)
      case _: AuthorisationException =>
        logger.warn(s"$logMsgStart - Unauthorised exception, redirect to GG sign in")
        Redirect(appConfig.signInUrl)
    }
  }

  private[predicates] def checkVatEnrolment[A](allEnrolments: Enrolments, block: User[A] => Future[Result])(implicit request: Request[A], messages: Messages) = {
    val logMsgStart: String = "[AuthPredicate][checkVatEnrolment]"
    val extractedMTDVATEnrolment: Option[String] = User.extractFirstMTDVatEnrolment(allEnrolments)
    if (extractedMTDVATEnrolment.isDefined) {
      val user: User[A] = User(extractedMTDVATEnrolment.get)
      block(user)
    } else {
      logger.debug(s"$logMsgStart - User does not have an activated HMRC-MTD-VAT enrolment. User had these enrolments: ${allEnrolments.enrolments}")
      Future(Forbidden(unauthorisedView()))
    }
  }

  private[predicates] def authoriseAsAgent[A](block: User[A] => Future[Result])
                                             (implicit request: Request[A], messages: Messages, hc: HeaderCarrier): Future[Result] = {

    val agentDelegatedAuthorityRule: String => Enrolment = vrn =>
      Enrolment(EnrolmentKeys.mtdITEnrolmentKey)
        .withIdentifier(EnrolmentKeys.mtdId, vrn)
        .withDelegatedAuthRule(EnrolmentKeys.agentDelegatedAuthRuleKey)

    request.session.get(SessionKeys.agentSessionVrn) match {
      case Some(vrn) =>
        authorised(agentDelegatedAuthorityRule(vrn)).retrieve(allEnrolments) {
          enrolments =>
            enrolments.enrolments.collectFirst {
              case Enrolment(EnrolmentKeys.agentEnrolmentKey, Seq(EnrolmentIdentifier(_, arn)), EnrolmentKeys.activated, _) => arn
            } match {
              case Some(arn) => block(User(vrn, arn = Some(arn)))
              case None =>
                logger.debug("[AuthPredicate][authoriseAsAgent] - Agent with no HMRC-AS-AGENT enrolment. Rendering unauthorised view.")
                Future.successful(Forbidden(unauthorisedView()))
            }
        } recover {
          case _: NoActiveSession =>
            logger.debug(s"AgentPredicate][authoriseAsAgent] - No active session. Redirecting to ${appConfig.signInUrl}")
            Redirect(appConfig.signInUrl)
          case _: AuthorisationException =>
            logger.debug(s"[AgentPredicate][authoriseAsAgent] - Agent does not have delegated authority for client. " +
              s"Showing unauthorised")
            Forbidden(unauthorisedView())
        }
      case None =>
        logger.debug(s"[AuthPredicate][authoriseAsAgent] - No Client MTDITID in session. Not Implemented")
        Future.successful(NotImplemented)
      // TODO - Find out which Income Tax V&C page to redirect to
    }
  }

  private[predicates] def isAgent(affinityGroup: AffinityGroup): Boolean = {
    affinityGroup == AffinityGroup.Agent
  }
}
