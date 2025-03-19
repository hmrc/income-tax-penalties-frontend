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
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.incometaxpenaltiesfrontend.connectors.PenaltiesConnector
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.compliance.ComplianceData
import uk.gov.hmrc.incometaxpenaltiesfrontend.utils.IncomeTaxSessionKeys
import uk.gov.hmrc.incometaxpenaltiesfrontend.utils.Logger.logger

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ComplianceService @Inject()(connector: PenaltiesConnector)(implicit ec: ExecutionContext) {

  def calculateComplianceWindow()(implicit request: Request[_]): Option[(LocalDate, LocalDate)] =
    request.session.get(IncomeTaxSessionKeys.pocAchievementDate).map(LocalDate.parse) match {
      case Some(pocDate) if pocDate.getYear == 9999 =>
        logger.info("[ComplianceService][calculateComplianceWindow] - User does not have a compliance window, PoC year was 9999")
        None
      case Some(pocDate) =>
        Some(pocDate.minusYears(2) -> pocDate)
      case None =>
        logger.info("[ComplianceService][calculateComplianceWindow] - User does not have a PoC Achievement Date in session")
        None
    }

  def getDESComplianceData(mtdItId: String,
                           startDate: LocalDate,
                           endDate: LocalDate)(implicit hc: HeaderCarrier): Future[Option[ComplianceData]] =
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
