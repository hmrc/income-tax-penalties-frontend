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

import fixtures.LSPDetailsTestData
import fixtures.messages.PenaltyTagStatusMessages
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Lang, Messages, MessagesApi}
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.appealInfo.{AppealInformationType, AppealLevelEnum, AppealStatusEnum}
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.lsp.LSPPenaltyStatusEnum.Inactive

class TagHelperSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite with LSPDetailsTestData {

  lazy val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  lazy val tagHelper: TagHelper = new TagHelper {}

  "TagHelper" when {

    Seq(PenaltyTagStatusMessages.English, PenaltyTagStatusMessages.Welsh).foreach { messagesForLanguage =>

      implicit val msgs: Messages = messagesApi.preferred(Seq(Lang(messagesForLanguage.lang.code)))

      s"rendering in language '${messagesForLanguage.lang.name}'" when {

        "calling .getTagStatus()" when {

          "provided with a Penalty Point (non-Financial)" when {

            "status is 'Active'" should {

              "generate an Active tag model with correct message and class" in {

                val tag = tagHelper.getTagStatus(sampleLateSubmissionPoint)

                tag.classes shouldBe ""
                tag.content shouldBe Text(messagesForLanguage.active)
              }
            }

            "status is 'Inactive'" when {

              "the appeal status is 'Upheld'" should {

                "generate an Upheld tag model with correct message and class" in {

                  val tag = tagHelper.getTagStatus(sampleLateSubmissionPoint.copy(
                    penaltyStatus = Inactive,
                    appealInformation = Some(Seq(
                      AppealInformationType(
                        appealStatus = Some(AppealStatusEnum.Upheld),
                        appealLevel = Some(AppealLevelEnum.HMRC)
                      )
                    ))
                  ))

                  tag.classes shouldBe ""
                  tag.content shouldBe Text(messagesForLanguage.upheld)
                }
              }

              "the appeal status is not 'Upheld'" should {

                "generate an Inactive tag model with correct message and class" in {

                  val tag = tagHelper.getTagStatus(sampleLateSubmissionPoint.copy(penaltyStatus = Inactive))

                  tag.classes shouldBe ""
                  tag.content shouldBe Text(messagesForLanguage.expired)
                }
              }
            }
          }

          "provided with a Financial Penalty" when {

            "status is 'Active'" when {

              "their is no outstanding amount" should {

                "generate a Paid tag model with correct message and class" in {

                  val tag = tagHelper.getTagStatus(sampleLateSubmissionPenaltyCharge.copy(
                    chargeOutstandingAmount = Some(BigDecimal(0))
                  ))

                  tag.classes shouldBe "govuk-tag--green"
                  tag.content shouldBe Text(messagesForLanguage.paid)
                }
              }

              "their is an outstanding amount, that is not paid at all" should {

                "generate a Due tag model with correct message and class with outstanding amount" in {

                  val tag = tagHelper.getTagStatus(sampleLateSubmissionPenaltyCharge)

                  tag.classes shouldBe "govuk-tag--red"
                  tag.content shouldBe Text(messagesForLanguage.due)
                }
              }

              "their is an outstanding amount, that is partially paid" should {

                "generate a Partially Paid tag model with correct message and class with outstanding amount" in {

                  val tag = tagHelper.getTagStatus(sampleLateSubmissionPenaltyCharge.copy(
                    chargeAmount = Some(BigDecimal(100)),
                    chargeOutstandingAmount = Some(BigDecimal(25.69))
                  ))

                  tag.classes shouldBe "govuk-tag--red"
                  tag.content shouldBe Text(messagesForLanguage.partiallyPaid("25.69"))
                }

                "when left to pay amount has .00 as decimals, then don't show pence" in {

                  val tag = tagHelper.getTagStatus(sampleLateSubmissionPenaltyCharge.copy(
                    chargeAmount = Some(BigDecimal(100)),
                    chargeOutstandingAmount = Some(BigDecimal(25))
                  ))

                  tag.classes shouldBe "govuk-tag--red"
                  tag.content shouldBe Text(messagesForLanguage.partiallyPaid("25"))
                }
              }
            }

            "status is 'Inactive'" when {

              "the appeal status is 'Upheld'" should {

                "generate an Upheld tag model with correct message and class" in {

                  val tag = tagHelper.getTagStatus(sampleLateSubmissionPenaltyCharge.copy(
                    penaltyStatus = Inactive,
                    appealInformation = Some(Seq(
                      AppealInformationType(
                        appealStatus = Some(AppealStatusEnum.Upheld),
                        appealLevel = Some(AppealLevelEnum.HMRC)
                      )
                    ))
                  ))

                  tag.classes shouldBe ""
                  tag.content shouldBe Text(messagesForLanguage.upheld)
                }
              }

              "the appeal status is not 'Upheld'" should {

                "generate an Inactive tag model with correct message and class" in {

                  val tag = tagHelper.getTagStatus(sampleLateSubmissionPenaltyCharge.copy(penaltyStatus = Inactive))

                  tag.classes shouldBe ""
                  tag.content shouldBe Text(messagesForLanguage.expired)
                }
              }
            }
          }
        }
      }
    }
  }
}
