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
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.penaltyDetails.lsp.{LSPDetails, LSPPenaltyStatusEnum}

import java.time.LocalDate


class LSPDetailsSpec extends AnyWordSpec with Matchers {

  "LSPDetails" should {
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
        TestLSPDetails.withAppealInfo(LSPPenaltyStatusEnum.Inactive, testAppealInfo).appealLevel shouldBe Some(AppealLevelEnum.SecondStageAppeal)
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
        TestLSPDetails.withAppealInfo(LSPPenaltyStatusEnum.Inactive, testAppealInfo).appealLevel shouldBe Some(AppealLevelEnum.SecondStageAppeal)
      }
    }
  }

  object TestLSPDetails {
    val penaltyNumber = "12345678901234"
    val creationDate: LocalDate = LocalDate.of(2021, 3, 7)
    val expiryDate: LocalDate = creationDate.plusYears(2)

    def withAppealInfo(penaltyStatus: LSPPenaltyStatusEnum.Value, appealInfo: Seq[AppealInformationType]): LSPDetails = LSPDetails(
      penaltyNumber = "12345678901234",
      penaltyOrder = None,
      penaltyCategory = None,
      penaltyStatus = penaltyStatus,
      penaltyCreationDate = creationDate,
      penaltyExpiryDate = expiryDate,
      communicationsDate = None,
      fapIndicator = None,
      lateSubmissions = None,expiryReason = None,
      appealInformation = Some(appealInfo),
      chargeDueDate = None,
      chargeOutstandingAmount = None,
      chargeAmount = None,
      triggeringProcess = None,
      chargeReference = None
    )
  }
}
