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
import uk.gov.hmrc.incometaxpenaltiesfrontend.utils.Logger.logger
import uk.gov.hmrc.incometaxpenaltiesfrontend.utils.PagerDutyHelper.PagerDutyKeys.POC_ACHIEVEMENT_DATE_NOT_FOUND
import uk.gov.hmrc.incometaxpenaltiesfrontend.utils.{IncomeTaxSessionKeys, PagerDutyHelper}

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ComplianceService @Inject()(connector: PenaltiesConnector) (implicit ec: ExecutionContext) {

  def getDESComplianceData(mtdItId: String,
                           pocAchievementDate: Option[LocalDate] = None
                          )(implicit hc: HeaderCarrier,
                            request: Request[_]): Future[Option[ComplianceData]] = {
    val pocAchievementDateFromSession: Option[LocalDate] = request.session.get(IncomeTaxSessionKeys.pocAchievementDate).map(LocalDate.parse(_))
    pocAchievementDate.orElse(pocAchievementDateFromSession) match {
      case Some(pocAchievementDate) => {
        val fromDate = pocAchievementDate.minusYears(2)
        connector.getObligationData(mtdItId, fromDate, pocAchievementDate).map {
          _.fold(
            failure => {
              logger.error(s"[ComplianceService][getDESComplianceData] - Connector failure: ${failure.message}")
              logger.error("[ComplianceService][getDESComplianceData] - Failed to retrieve obligation data, returning None back to controller (renders ISE)")
              None
            },
            obligationData => {
              logger.debug(s"[ComplianceService][getDESComplianceData] - Successful call to get obligation data,  obligation data = $obligationData")
              logger.info(s"[ComplianceService][getDESComplianceData] - Successful call to get obligation data.")
              Some(obligationData)
            }
          )
        }
      }
      case _ => {
        logger.error(s"[ComplianceService][getDESComplianceData] - POC Achievement date was not present in session")
        PagerDutyHelper.log("ComplianceService: getDESComplianceData", POC_ACHIEVEMENT_DATE_NOT_FOUND)
        Future.successful(None)
      }
    }
  }
}
