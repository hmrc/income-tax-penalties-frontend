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

package uk.gov.hmrc.incometaxpenaltiesfrontend.services

import play.api.mvc.Request
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}
import uk.gov.hmrc.incometaxpenaltiesfrontend.connectors.PenaltiesConnector
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.compliance.{ComplianceData, MandationStatus}
import uk.gov.hmrc.incometaxpenaltiesfrontend.utils.IncomeTaxSessionKeys
import uk.gov.hmrc.incometaxpenaltiesfrontend.utils.Logger.logger

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ComplianceService @Inject()(connector: PenaltiesConnector)(implicit ec: ExecutionContext) {

  def calculateComplianceWindow(mandationStatus: MandationStatus)(implicit request: Request[_]): Future[(LocalDate, LocalDate)] = {
    request.session.get(IncomeTaxSessionKeys.pocAchievementDate) match {
      case Some(pocDate) =>
        val parsedDate = LocalDate.parse(pocDate)
        if (parsedDate.getYear == 9999) {
          logger.error("[ComplianceService][calculateComplianceWindow] - User does not have a compliance window")
          throw new InternalServerException("[ComplianceService][calculateComplianceWindow] - User does not have any compliance issues - date was defaulted to '9999-12-31'")
        } else {
          mandationStatus.toString match {
            case "MTD Mandated" =>
              logger.info("[ComplianceService][calculateComplianceWindow] - User has a 12 month window due to mandation status")
              Future.successful( parsedDate.minusYears(1) -> parsedDate)
            case "MTD Voluntary" | "Annual" | "Non Digital" | "MTD Exempt" =>
              logger.info("[ComplianceService][calculateComplianceWindow] - User has a 24 month window due to mandation status")
              Future.successful(parsedDate.minusYears(2) -> parsedDate)
            case status =>
              throw new InternalServerException(s"Invalid Mandation Status - $status does not have a compliance window")
          }
        }
      case None =>
        logger.error("[ComplianceService][calculateComplianceWindow] - User does not have a compliance window")
        throw new InternalServerException("User does not have any compliance issues - no PoC Achievement Date was returned")
    }
    // TODO
    //  - confirm this logic with Andrew as both dates may end being specified
    //  - is mandation status still needed if that is the case?
    //  - handle the no compliance window scenarios gracefully, perhaps with an error page or redirection? Design input needed?

  }

  def getDESComplianceData(mtdItId: String,
                           startDate: LocalDate,
                           endDate: LocalDate
                          )(implicit hc: HeaderCarrier): Future[Option[ComplianceData]] =
    connector.getComplianceData(mtdItId, startDate, endDate).map {
      case Right(obligationData) =>
        logger.debug(s"[ComplianceService][getDESComplianceData] - Successful call to get obligation data,  obligation data = $obligationData")
        logger.info(s"[ComplianceService][getDESComplianceData] - Successful call to get obligation data.")
        Some(obligationData)
      case Left(failure) =>
        logger.error(s"[ComplianceService][getDESComplianceData] - Connector failure: ${failure.message}")
        logger.error("[ComplianceService][getDESComplianceData] - Failed to retrieve obligation data, returning None back to controller (renders ISE)")
        None
    }

}
