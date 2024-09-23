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

package controllers.testOnly

import connectors.SessionDataConnector
import connectors.SessionDataConnector.SessionData
import controllers.agent.SessionKeys.*
import play.api.Logging
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.auth.core.retrieve.{EmptyRetrieval, ~}
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{affinityGroup, confidenceLevel, internalId}
import uk.gov.hmrc.auth.core.{AuthConnector, Enrolment}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.Inject
import scala.concurrent.ExecutionContext
import scala.concurrent.Future.successful

class SetDelegationController @Inject()(
  authConnector: AuthConnector,
  val mcc: MessagesControllerComponents,
  sessionDataConnector: SessionDataConnector
)(implicit
  val ec: ExecutionContext
) extends FrontendController(mcc) with Logging {

  def delegationPage(): Action[AnyContent] = Action.async { request =>
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    (for (sessionData <- sessionDataConnector.getSessionData) yield {
      import sessionData._

      mtditid.filterNot(_.isBlank).map { id =>
        implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
        val spec = Enrolment("HMRC-MTD-IT").withIdentifier("MTDITID", id).withDelegatedAuthRule("mtd-it-auth")
        val retr = affinityGroup and internalId and confidenceLevel
        authConnector.authorise(spec, retr).map {
          case Some(_) ~ Some(_) ~ confidenceLevel if confidenceLevel.level >= 200 => s"Success (confidence $confidenceLevel)"
          case Some(_) ~ Some(_) ~ confidenceLevel => s"Failed: confidence = $confidenceLevel, required = 200"
          case affinityGroup ~ internalId ~ confidenceLevel => s"Failed: affinityGroup = $affinityGroup, internalId = $internalId, confidence = $confidenceLevel"
        }.recover(e => e.getClass.getSimpleName + ": " + e.getMessage)

      }.getOrElse(successful("")).map { authReesult =>
        logger.info(s"[SetDelegationController][setDelegation] Existing MTDITID=$mtditid, NINO=$nino, auth result: $authReesult")

        val view = views.html.testOnly.SetDelegation(mtditid.getOrElse(""), nino.getOrElse(""), sessionId.getOrElse("none"), authReesult)
        Ok(view)
      }
    }).flatten
  }
  
  def setDelegation(): Action[AnyContent] = Action.async { request =>
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    val data = request.body.asFormUrlEncoded.get
    val mtditid = data("mtditid").mkString
    val nino = data("nino").mkString

    logger.info(s"[SetDelegationController][setDelegation] Setting client MTDITID to $mtditid and NINO to $nino")

    sessionDataConnector.putSessionData(SessionData(
      mtditid = Some(mtditid),
      nino = Some(nino),
      utr = Some(""),
      sessionId = request.session.get("sessionId")
    )) map { _ =>
      SeeOther(routes.SetDelegationController.delegationPage().url)
    }
  }
}
