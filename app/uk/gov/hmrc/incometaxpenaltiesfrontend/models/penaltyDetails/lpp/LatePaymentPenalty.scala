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

package uk.gov.hmrc.incometaxpenaltiesfrontend.models.penaltyDetails.lpp

import play.api.libs.json.{Format, Json}
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.penaltyDetails.appealInfo.AppealStatusEnum

case class LatePaymentPenalty(lppDetails: Option[Seq[LPPDetails]], ManualLPPIndicator: Option[Boolean] = None
                             ) {

  val details = lppDetails.getOrElse(Seq.empty[LPPDetails])
  val withoutAppealedPenalties: Seq[LPPDetails] =
    details.filterNot(details => details.appealInformation.exists(_.exists(_.appealStatus.contains(AppealStatusEnum.Upheld))))
}

object LatePaymentPenalty {
  implicit val format: Format[LatePaymentPenalty] = Json.format[LatePaymentPenalty]
}
