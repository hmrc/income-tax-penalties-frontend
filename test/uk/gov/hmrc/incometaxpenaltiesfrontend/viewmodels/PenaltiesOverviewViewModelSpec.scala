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
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.Totalisations
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.appealInfo.{AppealLevelEnum, AppealStatusEnum}
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.lsp.LSPPenaltyStatusEnum
import uk.gov.hmrc.incometaxpenaltiesfrontend.viewModels.PenaltiesOverviewViewModel

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
                  content = Seq.empty,
                  hasFinancialCharge = false
                )
            }
          }

          "there are no penalties, but interest is accruing and the Income Tax charge has not been paid" should {

            "return a PenaltiesOverviewViewModel with no content" in {
              PenaltiesOverviewViewModel(samplePenaltyDetailsModel.copy(
                totalisations = Some(Totalisations(
                  LSPTotalValue = Some(200),
                  penalisedPrincipalTotal = Some(2000),
                  LPPPostedTotal = Some(165.25),
                  LPPEstimatedTotal = Some(15.26),
                  totalAccountOverdue = Some(389.26),
                  totalAccountPostedInterest = Some(5.50),
                  totalAccountAccruingInterest = Some(3.25)
                )),
                lateSubmissionPenalty = None,
                latePaymentPenalty = None
              )) shouldBe
                PenaltiesOverviewViewModel(
                  content = Seq(
                    messagesForLanguage.overviewOverdueTaxCharge,
                    messagesForLanguage.overviewInterest
                  ),
                  hasFinancialCharge = true
                )
            }
          }

          "there is one ACTIVE LSP Point" should {

            "return a PenaltiesOverviewViewModel with 1 penalty point content" in {
              PenaltiesOverviewViewModel(samplePenaltyDetailsModel.copy(latePaymentPenalty = None)) shouldBe
                PenaltiesOverviewViewModel(
                  content = Seq(messagesForLanguage.overviewLSPPoints(1)),
                  hasFinancialCharge = false
                )
            }
          }

          "there are two ACTIVE LSP Points" should {

            "return a PenaltiesOverviewViewModel with 2 penalty point content" in {

              val penaltyDetails = samplePenaltyDetailsModel
                .copy(
                  latePaymentPenalty = None,
                  lateSubmissionPenalty = samplePenaltyDetailsModel.lateSubmissionPenalty.map(_.copy(
                    summary = samplePenaltyDetailsModel.lateSubmissionPenalty.get.summary.copy(
                      activePenaltyPoints = 2
                    ),
                    details = Seq(
                      sampleLateSubmissionPoint,
                      sampleLateSubmissionPoint
                    )
                  ))
                )

              PenaltiesOverviewViewModel(penaltyDetails) shouldBe
                PenaltiesOverviewViewModel(
                  content = Seq(messagesForLanguage.overviewLSPPoints(2)),
                  hasFinancialCharge = false
                )
            }
          }

          "there are two ACTIVE LSPs, one INACTIVE and one appealed" should {

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
                      samplePenaltyPointAppeal(AppealStatusEnum.Upheld, AppealLevelEnum.HMRC)
                    )
                  ))
                )

              PenaltiesOverviewViewModel(penaltyDetails) shouldBe
                PenaltiesOverviewViewModel(
                  content = Seq(messagesForLanguage.overviewLSPPoints(2)),
                  hasFinancialCharge = false
                )
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
                      samplePenaltyPointAppeal(AppealStatusEnum.Upheld, AppealLevelEnum.HMRC)
                    )
                  ))
                )

              PenaltiesOverviewViewModel(penaltyDetails) shouldBe
                PenaltiesOverviewViewModel(
                  content = Seq(messagesForLanguage.overviewLSPPoints(2)),
                  hasFinancialCharge = false
                )
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

              PenaltiesOverviewViewModel(penaltyDetails) shouldBe
                PenaltiesOverviewViewModel(
                  content = Seq(
                    messagesForLanguage.overviewLSPFinancial(1),
                    messagesForLanguage.overviewLSPPointsMax
                  ),
                  hasFinancialCharge = true
                )
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

              PenaltiesOverviewViewModel(penaltyDetails) shouldBe
                PenaltiesOverviewViewModel(
                  content = Seq(
                    messagesForLanguage.overviewLSPFinancial(2),
                    messagesForLanguage.overviewLSPPointsMax
                  ),
                  hasFinancialCharge = true
                )
            }
          }

          "there is one ACTIVE LPP penalty (unpaid)" should {

            "return a PenaltiesOverviewViewModel with 1 LPP content AND hasFinancialCharge=true" in {
              PenaltiesOverviewViewModel(samplePenaltyDetailsModel.copy(lateSubmissionPenalty = None)) shouldBe
                PenaltiesOverviewViewModel(
                  content = Seq(messagesForLanguage.overviewLPP(1)),
                  hasFinancialCharge = true
                )
            }
          }

          "there are two ACTIVE LPP penalties (unpaid)" should {

            "return a PenaltiesOverviewViewModel with 2 LPP content AND hasFinancialCharge=true" in {
              PenaltiesOverviewViewModel(samplePenaltyDetailsModel.copy(
                lateSubmissionPenalty = None,
                latePaymentPenalty = samplePenaltyDetailsModel.latePaymentPenalty.map(_.copy(
                  details = Seq(
                    sampleUnpaidLPP1,
                    sampleUnpaidLPP1
                  )
                ))
              )) shouldBe
                PenaltiesOverviewViewModel(
                  content = Seq(messagesForLanguage.overviewLPP(2)),
                  hasFinancialCharge = true
                )
            }
          }

          "there are two ACTIVE LPP penalties (one paid, one unpaid)" should {

            "return a PenaltiesOverviewViewModel with 1 LPP content AND hasFinancialCharge=true" in {
              PenaltiesOverviewViewModel(samplePenaltyDetailsModel.copy(
                lateSubmissionPenalty = None,
                latePaymentPenalty = samplePenaltyDetailsModel.latePaymentPenalty.map(_.copy(
                  details = Seq(
                    sampleUnpaidLPP1,
                    samplePaidLPP1
                  )
                ))
              )) shouldBe
                PenaltiesOverviewViewModel(
                  content = Seq(messagesForLanguage.overviewLPP(1)),
                  hasFinancialCharge = true
                )
            }
          }

          "there is one ACTIVE LPP penalty and one Appealed LPP" should {

            "return a PenaltiesOverviewViewModel with 1 LPP content AND hasFinancialCharge=true" in {
              PenaltiesOverviewViewModel(samplePenaltyDetailsModel.copy(
                lateSubmissionPenalty = None,
                latePaymentPenalty = samplePenaltyDetailsModel.latePaymentPenalty.map(_.copy(
                  details = Seq(
                    sampleUnpaidLPP1,
                    sampleLPP1AppealUnpaid(AppealStatusEnum.Upheld, AppealLevelEnum.HMRC)
                  )
                ))
              )) shouldBe
                PenaltiesOverviewViewModel(
                  content = Seq(messagesForLanguage.overviewLPP(1)),
                  hasFinancialCharge = true
                )
            }
          }

          "there are no ACTIVE LPP (all paid)" should {

            "return a PenaltiesOverviewViewModel no content AND hasFinancialCharge=false" in {
              PenaltiesOverviewViewModel(samplePenaltyDetailsModel.copy(
                lateSubmissionPenalty = None,
                latePaymentPenalty = samplePenaltyDetailsModel.latePaymentPenalty.map(_.copy(
                  details = Seq(
                    samplePaidLPP1,
                    samplePaidLPP1
                  )
                ))
              )) shouldBe
                PenaltiesOverviewViewModel(
                  content = Seq(),
                  hasFinancialCharge = false
                )
            }
          }

          "there is a combination of interest, overdue tax, LSP points, LSP financial and LPP" should {

            "return a PenaltiesOverviewViewModel no content AND hasFinancialCharge=false" in {
              PenaltiesOverviewViewModel(samplePenaltyDetailsModel.copy(
                totalisations = Some(Totalisations(
                  LSPTotalValue = Some(200),
                  penalisedPrincipalTotal = Some(2000),
                  LPPPostedTotal = Some(165.25),
                  LPPEstimatedTotal = Some(15.26),
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
                  details = Seq(
                    sampleUnpaidLPP1,
                    sampleLPP2
                  )
                ))
              )) shouldBe
                PenaltiesOverviewViewModel(
                  content = Seq(
                    messagesForLanguage.overviewOverdueTaxCharge,
                    messagesForLanguage.overviewInterest,
                    messagesForLanguage.overviewLPP(2),
                    messagesForLanguage.overviewLSPFinancial(2),
                    messagesForLanguage.overviewLSPPointsMax
                  ),
                  hasFinancialCharge = true
                )
            }
          }
        }
      }
    }
  }
}
