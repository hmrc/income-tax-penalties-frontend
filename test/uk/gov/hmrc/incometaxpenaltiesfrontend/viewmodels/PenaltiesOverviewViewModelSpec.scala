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

package uk.gov.hmrc.incometaxpenaltiesfrontend.viewmodels

import fixtures.PenaltiesDetailsTestData
import fixtures.messages.IndexViewMessages
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Lang, Messages, MessagesApi}
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.penaltyDetails.Totalisations
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.penaltyDetails.appealInfo.{AppealLevelEnum, AppealStatusEnum}
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.penaltyDetails.lsp.LSPPenaltyStatusEnum
import uk.gov.hmrc.incometaxpenaltiesfrontend.viewModels.PenaltiesOverviewViewModel
import uk.gov.hmrc.incometaxpenaltiesfrontend.views.ViewUtils.pluralOrSingular

class PenaltiesOverviewViewModelSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite with PenaltiesDetailsTestData {

  lazy val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]

  "PenaltiesOverviewViewModel" when {

    Seq(IndexViewMessages.English, IndexViewMessages.Welsh).foreach { messagesForLanguage =>

      implicit val msgs: Messages = messagesApi.preferred(Seq(Lang(messagesForLanguage.lang.code)))

      s"when language is set to '${messagesForLanguage.lang.name}'" when {

        "calling .apply()" when {

          "there are no penalties" should {

            "return a PenaltiesOverviewViewModel with no content" in {
              PenaltiesOverviewViewModel(emptyPenaltyDetailsModel) shouldBe
                PenaltiesOverviewViewModel(
                  overviewItems = Seq.empty,
                  hasFinancialCharge = false
                )
            }
          }

          "there are no penalties, but interest is accruing and the Income Tax charge has not been paid" should {

            "return a PenaltiesOverviewViewModel with no content" in {

              val result =
                PenaltiesOverviewViewModel(samplePenaltyDetailsModel.copy(
                  totalisations = Some(Totalisations(
                    lspTotalValue = Some(200),
                    penalisedPrincipalTotal = Some(2000),
                    lppPostedTotal = Some(165.25),
                    lppEstimatedTotal = Some(15.26),
                    totalAccountOverdue = Some(389.26),
                    totalAccountPostedInterest = Some(5.50),
                    totalAccountAccruingInterest = Some(3.25)
                  )),
                  lateSubmissionPenalty = None,
                  latePaymentPenalty = None))

              result.overviewItems.map(oi => Messages(oi.messageKey(hasBullets = true, isAgent = false))) shouldBe Seq(
                messagesForLanguage.overviewOverdueTaxCharge,
                messagesForLanguage.overviewInterest
              )

              result.hasFinancialCharge shouldBe true
            }
          }

          "there are two ACTIVE LSP Points, one INACTIVE and one appealed" should {

            "return a PenaltiesOverviewViewModel with 2 penalty point content" in {

              val penaltyDetails = samplePenaltyDetailsModel
                .copy(
                  latePaymentPenalty = None,
                  lateSubmissionPenalty = samplePenaltyDetailsModel.lateSubmissionPenalty.map(_.copy(
                    summary = samplePenaltyDetailsModel.lateSubmissionPenalty.get.summary.copy(
                      activePenaltyPoints = 2,
                      inactivePenaltyPoints = 2
                    ),
                    details = Seq(
                      sampleLateSubmissionPoint,
                      sampleLateSubmissionPoint.copy(penaltyStatus = LSPPenaltyStatusEnum.Inactive),
                      sampleLateSubmissionPoint,
                      samplePenaltyPointAppeal(AppealStatusEnum.Upheld, AppealLevelEnum.FirstStageAppeal)
                    )
                  ))
                )

              val result = PenaltiesOverviewViewModel(penaltyDetails)

              result.overviewItems.map(oi => Messages(pluralOrSingular(oi.messageKey(hasBullets = false, isAgent = false), 2), 2)) shouldBe Seq(
                messagesForLanguage.overviewLSPPointsNoBullets(2)
              )

              result.hasFinancialCharge shouldBe false

            }
          }

          "there are four ACTIVE LSP Points, with the latest point being a financial charge" should {

            "return a PenaltiesOverviewViewModel with max penalty point content AND LSP penalty content AND hasFinancialCharge=true" in {

              val penaltyDetails = samplePenaltyDetailsModel
                .copy(
                  latePaymentPenalty = None,
                  lateSubmissionPenalty = samplePenaltyDetailsModel.lateSubmissionPenalty.map(_.copy(
                    summary = samplePenaltyDetailsModel.lateSubmissionPenalty.get.summary.copy(
                      activePenaltyPoints = 4,
                      inactivePenaltyPoints = 2
                    ),
                    details = Seq(
                      sampleLateSubmissionPoint,
                      sampleLateSubmissionPoint,
                      sampleLateSubmissionPoint,
                      sampleLateSubmissionPenaltyCharge
                    )
                  ))
                )

              val result = PenaltiesOverviewViewModel(penaltyDetails)

              val keys = result.overviewItems.map(_.messageKey(hasBullets = true, isAgent = false))
              Seq(
                Messages(pluralOrSingular(keys.head, 1), 1),
                Messages(keys(1))
              ) shouldBe Seq(
                messagesForLanguage.overviewLSPFinancial(1),
                messagesForLanguage.overviewLSPPointsMax
              )

              result.hasFinancialCharge shouldBe true

            }
          }

          "there are five ACTIVE LSP Points, with the latest two being a financial charge" should {

            "return a PenaltiesOverviewViewModel with max penalty point content AND LSP penalty content AND hasFinancialCharge=true" in {

              val penaltyDetails = samplePenaltyDetailsModel
                .copy(
                  latePaymentPenalty = None,
                  lateSubmissionPenalty = samplePenaltyDetailsModel.lateSubmissionPenalty.map(_.copy(
                    summary = samplePenaltyDetailsModel.lateSubmissionPenalty.get.summary.copy(
                      activePenaltyPoints = 5,
                      inactivePenaltyPoints = 2
                    ),
                    details = Seq(
                      sampleLateSubmissionPoint,
                      sampleLateSubmissionPoint,
                      sampleLateSubmissionPoint,
                      sampleLateSubmissionPenaltyCharge,
                      sampleLateSubmissionPenaltyCharge
                    )
                  ))
                )

              val result = PenaltiesOverviewViewModel(penaltyDetails)


              val keys = result.overviewItems.map(_.messageKey(hasBullets = true, isAgent = false))
              Seq(
                Messages(pluralOrSingular(keys.head, 2), 2),
                Messages(keys(1))
              ) shouldBe Seq(
                messagesForLanguage.overviewLSPFinancial(2),
                messagesForLanguage.overviewLSPPointsMax
              )

              result.hasFinancialCharge shouldBe true

            }
          }

          "there is one ACTIVE LPP penalty (unpaid)" should {

            "return a PenaltiesOverviewViewModel with 1 LPP content AND hasFinancialCharge=true" in {

              val penaltyDetails = samplePenaltyDetailsModel.copy(lateSubmissionPenalty = None)
              val result = PenaltiesOverviewViewModel(penaltyDetails)

              result.overviewItems.map(oi => Messages(pluralOrSingular(oi.messageKey(hasBullets = false, isAgent = false), 1), 1)) shouldBe Seq(
                messagesForLanguage.overviewLPPNoBullets(1)
              )

              result.hasFinancialCharge shouldBe true

            }
          }

          "there are two ACTIVE LPP penalties (unpaid)" should {

            "return a PenaltiesOverviewViewModel with 2 LPP content AND hasFinancialCharge=true" in {
              val penaltyDetails = samplePenaltyDetailsModel.copy(
                lateSubmissionPenalty = None,
                latePaymentPenalty = samplePenaltyDetailsModel.latePaymentPenalty.map(_.copy(
                  lppDetails = Some(Seq(
                    sampleUnpaidLPP1,
                    sampleUnpaidLPP1
                  ))
                ))
              )
              val result = PenaltiesOverviewViewModel(penaltyDetails)


              result.overviewItems.map(oi => Messages(pluralOrSingular(oi.messageKey(hasBullets = false, isAgent = false), 2), 2)) shouldBe Seq(
                messagesForLanguage.overviewLPPNoBullets(2)
              )

              result.hasFinancialCharge shouldBe true

            }
          }

          "there are two ACTIVE LPP penalties (one paid, one unpaid)" should {

            "return a PenaltiesOverviewViewModel with 1 LPP content AND hasFinancialCharge=true" in {
              val penaltyDetails = samplePenaltyDetailsModel.copy(
                lateSubmissionPenalty = None,
                latePaymentPenalty = samplePenaltyDetailsModel.latePaymentPenalty.map(_.copy(
                  lppDetails = Some(Seq(
                    sampleUnpaidLPP1,
                    samplePaidLPP1
                  ))
                ))
              )

              val result = PenaltiesOverviewViewModel(penaltyDetails)


              result.overviewItems.map(oi => Messages(pluralOrSingular(oi.messageKey(hasBullets = false, isAgent = false), 1), 1)) shouldBe Seq(
                messagesForLanguage.overviewLPPNoBullets(1)
              )

              result.hasFinancialCharge shouldBe true

            }
          }

          "there is one ACTIVE LPP penalty and one Appealed LPP" should {

            "return a PenaltiesOverviewViewModel with 1 LPP content AND hasFinancialCharge=true" in {
              val penaltyDetails = (samplePenaltyDetailsModel.copy(
                lateSubmissionPenalty = None,
                latePaymentPenalty = samplePenaltyDetailsModel.latePaymentPenalty.map(_.copy(
                  lppDetails = Some(Seq(
                    sampleUnpaidLPP1,
                    sampleLPP1AppealUnpaid(AppealStatusEnum.Upheld, AppealLevelEnum.FirstStageAppeal)
                  )
                  ))
                )))

              val result = PenaltiesOverviewViewModel(penaltyDetails)


              result.overviewItems.map(oi => Messages(pluralOrSingular(oi.messageKey(hasBullets = false, isAgent = false), 1), 1)) shouldBe Seq(
                messagesForLanguage.overviewLPPNoBullets(1)
              )

              result.hasFinancialCharge shouldBe true

            }
          }

          "there are no ACTIVE LPP (all paid)" should {

            "return a PenaltiesOverviewViewModel no content AND hasFinancialCharge=false" in {
              val penaltyDetails = samplePenaltyDetailsModel.copy(
                lateSubmissionPenalty = None,
                latePaymentPenalty = samplePenaltyDetailsModel.latePaymentPenalty.map(_.copy(
                  lppDetails = Some(Seq(
                    samplePaidLPP1,
                    samplePaidLPP1
                  )
                  )))
              )

              val result = PenaltiesOverviewViewModel(penaltyDetails)

              result.overviewItems.map(oi => Messages(oi.messageKey(hasBullets = false, isAgent = false))) shouldBe Seq.empty

              result.hasFinancialCharge shouldBe false
            }
          }

          "there is a combination of interest, overdue tax, LSP points, LSP financial and LPP" should {

            "return a PenaltiesOverviewViewModel no content AND hasFinancialCharge=false" in {
              val penaltyDetails = samplePenaltyDetailsModel.copy(
                totalisations = Some(Totalisations(
                  lspTotalValue = Some(200),
                  penalisedPrincipalTotal = Some(2000),
                  lppPostedTotal = Some(165.25),
                  lppEstimatedTotal = Some(15.26),
                  totalAccountOverdue = Some(389.26),
                  totalAccountPostedInterest = Some(5.50),
                  totalAccountAccruingInterest = Some(3.25)
                )),
                lateSubmissionPenalty = samplePenaltyDetailsModel.lateSubmissionPenalty.map(_.copy(
                  summary = samplePenaltyDetailsModel.lateSubmissionPenalty.get.summary.copy(
                    activePenaltyPoints = 5,
                    inactivePenaltyPoints = 2
                  ),
                  details = Seq(
                    sampleLateSubmissionPoint,
                    sampleLateSubmissionPoint,
                    sampleLateSubmissionPoint,
                    sampleLateSubmissionPenaltyCharge,
                    sampleLateSubmissionPenaltyCharge
                  )
                )),
                latePaymentPenalty = samplePenaltyDetailsModel.latePaymentPenalty.map(_.copy(
                  lppDetails = Some(Seq(
                    sampleUnpaidLPP1,
                    sampleLPP2
                  )
                  )))
              )

              val result = PenaltiesOverviewViewModel(penaltyDetails)

              val keys = result.overviewItems.map(_.messageKey(hasBullets = true, isAgent = false))
              Seq(
                Messages(keys.head),
                Messages(keys(1)),
                Messages(pluralOrSingular(keys(2), 2), 2),
                Messages(pluralOrSingular(keys(3), 2), 2),
                Messages(keys(4))
              ) shouldBe Seq(
                messagesForLanguage.overviewOverdueTaxCharge,
                messagesForLanguage.overviewInterest,
                messagesForLanguage.overviewLPP(2),
                messagesForLanguage.overviewLSPFinancial(2),
                messagesForLanguage.overviewLSPPointsMax
              )

              result.hasFinancialCharge shouldBe true
              
            }
          }
        }
      }
    }
  }
}
