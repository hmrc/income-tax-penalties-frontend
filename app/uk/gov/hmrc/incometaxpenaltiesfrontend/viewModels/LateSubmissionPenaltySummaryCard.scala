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

package uk.gov.hmrc.incometaxpenaltiesfrontend.viewModels

import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.govukfrontend.views.viewmodels.tag.Tag
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.penaltyDetails.appealInfo.{AppealLevelEnum, AppealStatusEnum}
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.penaltyDetails.lsp.LSPPenaltyCategoryEnum

case class LateSubmissionPenaltySummaryCard(
                                             cardRows: Seq[SummaryListRow],
                                             cardTitle: String,
                                             status: Tag,
                                             penaltyPoint: String,
                                             penaltyId: String,
                                             isReturnSubmitted: Boolean,
                                             penaltyCategory: Option[LSPPenaltyCategoryEnum.Value],
                                             totalPenaltyAmount: BigDecimal = 0,
                                             isAddedPoint: Boolean = false,
                                             isAppealedPoint: Boolean = false,
                                             appealStatus: Option[AppealStatusEnum.Value] = None,
                                             appealLevel: Option[AppealLevelEnum.Value] = None,
                                             isAddedOrRemovedPoint: Boolean = false,
                                             isManuallyRemovedPoint: Boolean = false,
                                             dueDate: Option[String]
                                           )
