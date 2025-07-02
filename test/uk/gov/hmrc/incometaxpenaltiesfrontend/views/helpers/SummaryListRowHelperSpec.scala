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

import fixtures.messages.AppealStatusMessages
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.Aliases.{HtmlContent, SummaryListRow, Text, Value}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.Key
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.penaltyDetails.appealInfo.{AppealLevelEnum, AppealStatusEnum}

class SummaryListRowHelperSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite {

  lazy val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  lazy val summaryListRowHelper: SummaryListRowHelper = new SummaryListRowHelper {}

  "SummaryListRowHelper" when {

    "calling .appealStatusRow()" when {

      Seq(AppealStatusMessages.English, AppealStatusMessages.Welsh).foreach { appealStatusMessages =>

        implicit val msgs: Messages = messagesApi.preferred(Seq(Lang(appealStatusMessages.lang.code)))

        s"when language is set to '${appealStatusMessages.lang.name}'" when {

          "there is an appeal in progress" when {

            "appeal status is NOT 'Unappealable'" should {

              "construct a SummaryListRow model with the Appeal status with expected messages" in {

                summaryListRowHelper.appealStatusRow(
                  Some(AppealStatusEnum.Under_Appeal),
                  Some(AppealLevelEnum.FirstStageAppeal)
                ) shouldBe
                  Some(summaryListRowHelper.summaryListRow(
                    label = appealStatusMessages.appealStatusKey,
                    value = Html(appealStatusMessages.underReviewHMRC)
                  ))
              }
            }

            "appeal status is 'Unappealable'" should {

              "return None" in {

                summaryListRowHelper.appealStatusRow(
                  Some(AppealStatusEnum.Unappealable),
                  Some(AppealLevelEnum.FirstStageAppeal)
                ) shouldBe None
              }
            }
          }

          "there is NO appeal in progress" when {

            "return None" in {
              summaryListRowHelper.appealStatusRow(None, None) shouldBe None
            }
          }
        }
      }
    }

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
