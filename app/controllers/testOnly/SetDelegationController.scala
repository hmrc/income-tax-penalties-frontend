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

import controllers.agent.SessionKeys.*
import play.api.Logging
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.auth.core.retrieve.EmptyRetrieval
import uk.gov.hmrc.auth.core.{AuthConnector, Enrolment}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.Inject
import scala.concurrent.ExecutionContext
import scala.concurrent.Future.successful

class SetDelegationController @Inject()(
  authConnector: AuthConnector,
  val mcc: MessagesControllerComponents
)(implicit
  val ec: ExecutionContext
) extends FrontendController(mcc) with Logging {

  def delegationPage(): Action[AnyContent] = Action.async { request =>
    val sessionId = request.session.get("sessionId")
    val mtditid = request.session.get(clientMTDID)
    val nino = request.session.get(clientNino) // eg TT217906A

    mtditid.filterNot(_.isBlank).map { id =>
      implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
      val spec = Enrolment("HMRC-MTD-IT").withIdentifier("MTDITID",id).withDelegatedAuthRule("mtd-it-auth")
      authConnector.authorise(spec, EmptyRetrieval).map(_=>"Success").recover(e=>e.getClass.getSimpleName + ": " + e.getMessage)

    }.getOrElse(successful("")).map { authReesult =>
      logger.info(s"[SetDelegationController][setDelegation] Existing MTDITID=$mtditid, NINO=$nino, auth result: $authReesult")

      val view = views.html.testOnly.SetDelegation(mtditid.getOrElse(""), nino.getOrElse(""), sessionId.getOrElse("none"), authReesult)
      Ok(view)
    }
  }
  
  def setDelegation(): Action[AnyContent] = Action { request =>
    val data = request.body.asFormUrlEncoded.get
    val mtditid = data("mtditid").mkString
    val nino = data("nino").mkString

    logger.info(s"[SetDelegationController][setDelegation] Setting client MTDITID to $mtditid and NINO to $nino")

    val newSession = request.session + (clientMTDID -> mtditid) + (clientNino -> nino)
    SeeOther(routes.SetDelegationController.delegationPage().url).withSession(newSession)
  }
}
