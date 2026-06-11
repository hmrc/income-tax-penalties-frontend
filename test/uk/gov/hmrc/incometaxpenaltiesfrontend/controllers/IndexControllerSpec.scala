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

package uk.gov.hmrc.incometaxpenaltiesfrontend.controllers

import fixtures.PenaltiesFixture
import org.scalatest.matchers.should
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.test.Injecting
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.penaltyDetails.lsp.LSPPenaltyStatusEnum

import java.time.LocalDate

class IndexControllerSpec extends AnyWordSpec with should.Matchers with GuiceOneAppPerSuite with Injecting with PenaltiesFixture {

  lazy val controller: IndexController = app.injector.instanceOf[IndexController]

  "sortPointsInDescendingOrder" should {

    "order points by penalty creation date, most recent obligation first" when {

      "the upstream penalty order does not match obligation chronology (e.g. after a breathing space filing frequency change)" in {
        // Quarterly points adjusted to annual after breathing space: 2 active (most recent), 2 expired (oldest).
        // The upstream penaltyOrder no longer matches the obligation chronology.
        val q1Expired = sampleLateSubmissionPoint.copy(
          penaltyNumber = "Q1",
          penaltyOrder = Some("1"),
          penaltyStatus = LSPPenaltyStatusEnum.Inactive,
          penaltyCreationDate = LocalDate.of(2027, 2, 7)
        )
        val q2Expired = sampleLateSubmissionPoint.copy(
          penaltyNumber = "Q2",
          penaltyOrder = Some("3"),
          penaltyStatus = LSPPenaltyStatusEnum.Inactive,
          penaltyCreationDate = LocalDate.of(2027, 5, 7)
        )
        val q3Active = sampleLateSubmissionPoint.copy(
          penaltyNumber = "Q3",
          penaltyOrder = Some("2"),
          penaltyStatus = LSPPenaltyStatusEnum.Active,
          penaltyCreationDate = LocalDate.of(2027, 8, 7)
        )
        val q4Active = sampleLateSubmissionPoint.copy(
          penaltyNumber = "Q4",
          penaltyOrder = Some("4"),
          penaltyStatus = LSPPenaltyStatusEnum.Active,
          penaltyCreationDate = LocalDate.of(2027, 11, 7)
        )

        val result = controller.sortPointsInDescendingOrder(Seq(q4Active, q2Expired, q3Active, q1Expired))

        result.map(_.penaltyNumber) shouldBe Seq("Q4", "Q3", "Q2", "Q1")
      }
    }

    "fall back to penalty order as a tie-breaker when creation dates are equal" in {
      val sharedDate = LocalDate.of(2027, 2, 7)
      val first = sampleLateSubmissionPoint.copy(penaltyNumber = "first", penaltyOrder = Some("1"), penaltyCreationDate = sharedDate)
      val second = sampleLateSubmissionPoint.copy(penaltyNumber = "second", penaltyOrder = Some("2"), penaltyCreationDate = sharedDate)
      val third = sampleLateSubmissionPoint.copy(penaltyNumber = "third", penaltyOrder = Some("3"), penaltyCreationDate = sharedDate)

      val result = controller.sortPointsInDescendingOrder(Seq(first, third, second))

      result.map(_.penaltyNumber) shouldBe Seq("third", "second", "first")
    }
  }
}
