/*
 * Copyright 2026 HM Revenue & Customs
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

package uk.gov.hmrc.incometaxpenaltiesfrontend.utils

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.penaltyDetails.lsp.LateSubmission

import java.time.LocalDate

class PenaltyPeriodHelperSpec extends AnyWordSpec with Matchers {
  val lateSubmission: LateSubmission = LateSubmission(
    lateSubmissionID = "001",
    incomeSource = None,
    taxPeriod = None,
    taxPeriodStartDate = None,
    taxPeriodEndDate = None,
    taxPeriodDueDate = None,
    returnReceiptDate = None,
    taxReturnStatus = None
  )
  "sortByPenaltyStartDate" should {
    "return a negative number when the first date is earlier" in {
      val p1 = lateSubmission.copy(taxPeriodStartDate = Some(LocalDate.of(2024, 1, 1)))
      val p2 = lateSubmission.copy(taxPeriodStartDate = Some(LocalDate.of(2024, 2, 1)))
      PenaltyPeriodHelper.sortByPenaltyStartDate(p1, p2) should be < 0
    }
    "return a positive number when the first date is later" in {
      val p1 = lateSubmission.copy(taxPeriodStartDate = Some(LocalDate.of(2024, 2, 1)))
      val p2 = lateSubmission.copy(taxPeriodStartDate = Some(LocalDate.of(2024, 1, 1)))
      PenaltyPeriodHelper.sortByPenaltyStartDate(p1, p2) should be > 0
    }
    "return 0 when the first submission has no taxPeriodStartDate" in {
      val p1 = lateSubmission.copy(taxPeriodStartDate = None)
      val p2 = lateSubmission.copy(taxPeriodStartDate = Some(LocalDate.of(2024, 1, 1)))
      PenaltyPeriodHelper.sortByPenaltyStartDate(p1, p2) shouldBe 0
    }
    "return 0 when the second submission has no taxPeriodStartDate" in {
      val p1 = lateSubmission.copy(taxPeriodStartDate = Some(LocalDate.of(2024, 1, 1)))
      val p2 = lateSubmission.copy(taxPeriodStartDate = None)
      PenaltyPeriodHelper.sortByPenaltyStartDate(p1, p2) shouldBe 0
    }
    "return 0 when neither submission has a taxPeriodStartDate" in {
      val p1 = lateSubmission.copy(taxPeriodStartDate = None)
      val p2 = lateSubmission.copy(taxPeriodStartDate = None)
      PenaltyPeriodHelper.sortByPenaltyStartDate(p1, p2) shouldBe 0
    }
  }

}
