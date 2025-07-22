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

package uk.gov.hmrc.incometaxpenaltiesfrontend.controllers.auth.actions

import play.api.mvc._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.incometaxpenaltiesfrontend.config.ErrorHandler
import uk.gov.hmrc.incometaxpenaltiesfrontend.controllers.auth.models.{AuthenticatedUserWithPenaltyData, CurrentUserRequest}
import uk.gov.hmrc.incometaxpenaltiesfrontend.services.PenaltiesService
import uk.gov.hmrc.incometaxpenaltiesfrontend.utils.Logger.logger
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PenaltyDataAction @Inject()(val penaltiesService: PenaltiesService,
                                  errorHandler: ErrorHandler,
                                  mcc: MessagesControllerComponents) extends ActionRefiner[CurrentUserRequest, AuthenticatedUserWithPenaltyData] {

  implicit val executionContext: ExecutionContext = mcc.executionContext

  override def refine[A](request: CurrentUserRequest[A]): Future[Either[Result, AuthenticatedUserWithPenaltyData[A]]] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
    implicit val _req: CurrentUserRequest[A] = request
    penaltiesService.getPenaltyDataForUser().flatMap(_.fold(
      error => {
        logger.error(s"[PenaltyDataAction][refine] Received error with message ${error.message} rendering ISE")
        errorHandler.showInternalServerError().map(errorView => Left(errorView))
      },
      penaltyDetails => Future.successful(Right(
        AuthenticatedUserWithPenaltyData(request.mtdItId, request.nino, penaltyDetails, request.arn, request.navBar)
      ))
    ))
  }
}
