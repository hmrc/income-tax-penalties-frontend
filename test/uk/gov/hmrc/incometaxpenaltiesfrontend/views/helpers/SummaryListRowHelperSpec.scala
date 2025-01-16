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

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.Aliases.{HtmlContent, SummaryListRow, Text, Value}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.Key

class SummaryListRowHelperSpec extends AnyWordSpec with Matchers {

  lazy val summaryListRowHelper: SummaryListRowHelper = new SummaryListRowHelper {}

  "SummaryListRowHelper" when {

    "calling .summaryListRow()" should {

      "construct a SummaryListRow model" in {

        summaryListRowHelper.summaryListRow("label", Html("<span>foo</span>")) shouldBe
          SummaryListRow(
            key = Key(
              content = Text("label"),
              classes = "govuk-summary-list__key"
            ),
            value = Value(
              content = HtmlContent(Html("<span>foo</span>")),
              classes = "govuk-summary-list__value"
            ),
            classes = "govuk-summary-list__row"
          )
      }
    }
  }
}
