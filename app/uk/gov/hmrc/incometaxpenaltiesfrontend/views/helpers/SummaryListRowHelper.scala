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

package uk.gov.hmrc.incometaxpenaltiesfrontend.views.helpers

import play.api.i18n.Messages
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryListRow, Value}
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.penaltyDetails.appealInfo.{AppealLevelEnum, AppealStatusEnum}

trait SummaryListRowHelper {

  def appealStatusRow(appealStatus: Option[AppealStatusEnum.Value],
                      appealLevel: Option[AppealLevelEnum.Value])(implicit messages: Messages): Option[SummaryListRow] =
    (appealStatus, appealLevel) match {
      case (Some(status), Some(level)) if status != AppealStatusEnum.Unappealable =>
        Some(summaryListRow(
          label = messages("appealStatus.key"),
          value = Html(messages(s"appealStatus.$status.$level"))
        ))
      case _ =>
        None
    }

  def summaryListRow(label: String, value: Html): SummaryListRow = SummaryListRow(
    key = Key(
      content = Text(label),
      classes = "govuk-summary-list__key"
    ),
    value = Value(
      content = HtmlContent(value),
      classes = "govuk-summary-list__value"
    ),
    classes = "govuk-summary-list__row"
  )
}
