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

package uk.gov.hmrc.incometaxpenaltiesfrontend.models.lsp

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.penaltyDetails.appealInfo.{AppealInformationType, AppealLevelEnum, AppealStatusEnum}
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.penaltyDetails.lpp.{LPPDetails, LPPDetailsMetadata, LPPPenaltyCategoryEnum, LPPPenaltyStatusEnum}

import java.time.LocalDate

class LPPDetailsSpec extends AnyWordSpec with Matchers {

  "LPPDetails" should {
    "obtain the correct appeal level" when {
      "the appeal information is ordered" in {
        val testAppealInfo = Seq(
          AppealInformationType(
            Some(AppealStatusEnum.Rejected),
            Some(AppealLevelEnum.SecondStageAppeal)
          ),
          AppealInformationType(
            Some(AppealStatusEnum.Rejected),
            Some(AppealLevelEnum.FirstStageAppeal)
          )
        )
        TestLPPDetails.withAppealInfo(LPPPenaltyStatusEnum.Posted, testAppealInfo).appealLevel shouldBe Some(AppealLevelEnum.SecondStageAppeal)
      }
      "the appeal information is not ordered" in {
        val testAppealInfo = Seq(
          AppealInformationType(
            Some(AppealStatusEnum.Rejected),
            Some(AppealLevelEnum.FirstStageAppeal)
          ),
          AppealInformationType(
            Some(AppealStatusEnum.Rejected),
            Some(AppealLevelEnum.SecondStageAppeal)
          )
        )
        TestLPPDetails.withAppealInfo(LPPPenaltyStatusEnum.Posted, testAppealInfo).appealLevel shouldBe Some(AppealLevelEnum.SecondStageAppeal)
      }
    }
  }

  object TestLPPDetails {
    val principleChargeBillingStartDate: LocalDate = LocalDate.of(2021, 5, 1) //2021-05-01 All other dates based off this date
    val principleChargeBillingEndDate: LocalDate = principleChargeBillingStartDate.plusMonths(1) //2021-06-01
    val principleChargeBillingDueDate: LocalDate = principleChargeBillingEndDate.plusDays(6) //2021-06-07
    val principleChargeRef = "12345678901234"
    val penaltyAmountAccruing: BigDecimal = BigDecimal(100)
    val penaltyAmountPosted: BigDecimal = BigDecimal(100)
    val lppMetadata: LPPDetailsMetadata = LPPDetailsMetadata(
      principalChargeMainTr = "4700",
      timeToPay = None
    )

    def withAppealInfo(penaltyStatus: LPPPenaltyStatusEnum.Value, appealInformationType: Seq[AppealInformationType]): LPPDetails = LPPDetails(
      principalChargeReference = principleChargeRef,
      penaltyCategory = LPPPenaltyCategoryEnum.LPP1,
      penaltyStatus = penaltyStatus,
      penaltyAmountAccruing = penaltyAmountAccruing,
      penaltyAmountPosted = penaltyAmountPosted,
      penaltyAmountPaid = None,
      penaltyAmountOutstanding = None,
      lpp1LRCalculationAmt = None,
      lpp1LRDays = None,
      lpp1LRPercentage = None,
      lpp1HRCalculationAmt = None,
      lpp1HRDays = None,
      lpp1HRPercentage = None,
      lpp2Days = None,
      lpp2Percentage = None,
      penaltyChargeCreationDate = None,
      communicationsDate = None,
      penaltyChargeReference = None,
      penaltyChargeDueDate = None,
      appealInformation = Some(appealInformationType),
      principalChargeBillingFrom = principleChargeBillingStartDate,
      principalChargeBillingTo = principleChargeBillingEndDate,
      principalChargeDueDate = principleChargeBillingDueDate,
      principalChargeLatestClearing = None,
      vatOutstandingAmount = None,
      metadata = lppMetadata
    )
  }
}