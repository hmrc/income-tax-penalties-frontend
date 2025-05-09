/*
 * Copyright 2025 HM Revenue & Customs
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

import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.incometaxpenaltiesfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesfrontend.connectors.MessageCountConnector
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.AuthenticatedUserRequest
import uk.gov.hmrc.incometaxpenaltiesfrontend.services.BtaNavBarService
import uk.gov.hmrc.incometaxpenaltiesfrontend.utils.IncomeTaxSessionKeys
import uk.gov.hmrc.incometaxpenaltiesfrontend.utils.Logger.logger
import uk.gov.hmrc.incometaxpenaltiesfrontend.views.html.navBar.PtaNavBar
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class NavBarRetrievalAction @Inject()(val messageCountConnector: MessageCountConnector,
                                      val btaNavBarService: BtaNavBarService,
                                      val ptaNavBar: PtaNavBar)
                                     (implicit val appConfig: AppConfig,
                                      val executionContext: ExecutionContext,
                                      val messagesApi: MessagesApi) extends ActionRefiner[AuthenticatedUserRequest, AuthenticatedUserRequest] with I18nSupport {

  override def refine[A](request: AuthenticatedUserRequest[A]): Future[Either[Result, AuthenticatedUserRequest[A]]] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
    implicit val _req: AuthenticatedUserRequest[A] = request

    if (request.isAgent) Future.successful(Right(request)) else {
      request.session.get(IncomeTaxSessionKeys.origin) match {
        case Some("PTA") => handlePtaNavBar()
        case Some("BTA") => handleBtaNavBar()
        case _ =>
          logger.info("[NavBarRetrievalAction][refine] No origin found in session, not constructing a Nav Bar as can't determine PTA or BTA")
          Future.successful(Right(request))
      }
    }
  }

  private def handlePtaNavBar[A]()(implicit request: AuthenticatedUserRequest[A], hc: HeaderCarrier): Future[Either[Result, AuthenticatedUserRequest[A]]] = {
    messageCountConnector.getMessageCount().map {
      case Right(count) =>
        Right(request.addNavBar(ptaNavBar(count.count)))
      case _ =>
        logger.warn("[NavBarRetrievalAction][refine] Failed to retrieve message count from 'message' microservice, continuing with 0 messages to continue gracefully")
        Right(request.addNavBar(ptaNavBar(0)))
    }
  }

  private def handleBtaNavBar[A]()(implicit request: AuthenticatedUserRequest[A], hc: HeaderCarrier): Future[Right[Result, AuthenticatedUserRequest[A]]] =
    btaNavBarService.retrieveBtaLinksAndRenderNavBar().map(btaNavBarHtml =>
      Right(btaNavBarHtml.fold(request)(content => request.addNavBar(content)))
    )
}
