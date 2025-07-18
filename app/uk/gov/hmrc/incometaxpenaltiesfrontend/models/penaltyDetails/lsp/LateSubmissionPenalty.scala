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

package uk.gov.hmrc.incometaxpenaltiesfrontend.models.penaltyDetails.lsp

import play.api.libs.functional.syntax.{toFunctionalBuilderOps, unlift}
import play.api.libs.json.{JsPath, OFormat, OWrites, Reads}
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.penaltyDetails.appealInfo.AppealStatusEnum

case class LateSubmissionPenalty(summary: LSPSummary,
                                 details: Seq[LSPDetails]) {

  val withoutAppealedPenalties: Seq[LSPDetails] =
    details.filterNot(details => details.appealInformation.exists(_.exists(_.appealStatus.contains(AppealStatusEnum.Upheld))))
}

object LateSubmissionPenalty {
  implicit val reads: Reads[LateSubmissionPenalty] = (
    (JsPath \ "lspSummary").read[LSPSummary] and
      (JsPath \ "lspDetails").read[Seq[LSPDetails]]
    )(LateSubmissionPenalty.apply _)


  implicit val writes: OWrites[LateSubmissionPenalty] = (
    (JsPath \ "lspSummary").write[LSPSummary] and
      (JsPath \ "lspDetails").write[Seq[LSPDetails]]
    )(unlift(LateSubmissionPenalty.unapply))

  implicit val format: OFormat[LateSubmissionPenalty] = OFormat(reads, writes)
}
