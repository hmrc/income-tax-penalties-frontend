/*
 * Copyright 2024 HM Revenue & Customs
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

package uk.gov.hmrc.incometaxpenaltiesfrontend.services

import fixtures.ComplianceDataTestData
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Messages, MessagesApi}
import play.api.test.FakeRequest
import uk.gov.hmrc.incometaxpenaltiesfrontend.utils.{DateFormatter, TimeMachine}
import uk.gov.hmrc.incometaxpenaltiesfrontend.viewModels.TimelineEvent

import java.time.LocalDate

class TimelineBuilderServiceSpec extends AnyWordSpec with Matchers with ComplianceDataTestData with GuiceOneAppPerSuite with DateFormatter {

object FakeTimeMachine extends TimeMachine {
  override def getCurrentDate: LocalDate = LocalDate.parse("2023-04-13")
}
    val service: TimelineBuilderService = new TimelineBuilderService(FakeTimeMachine)
    lazy val messages: Messages = app.injector.instanceOf[MessagesApi].preferred(FakeRequest())

  "buildTimeline" should {
    "return a sequence of timeline events " when {
      "there is one open event" in {
        val result = service.buildTimeline(sampleCompliancePayload)(messages)
        val testDate = dateToString(LocalDate.of(1920, 2, 29))(messages)
        val expected = Seq(TimelineEvent(s"Quarter: $testDate to $testDate", s"Due on $testDate. Send this missing submission now.", Some("Late")))
        result shouldBe expected
      }

      "there are two open events" in {
        val result = service.buildTimeline(sampleCompliancePayloadTwoEvents)(messages)
        val testDate = dateToString(LocalDate.of(1920, 2, 29))(messages)
        val testDateTwo = dateToString(LocalDate.of(1921, 2, 20))(messages)
        val expected = Seq(TimelineEvent(s"Quarter: $testDate to $testDate", s"Due on $testDate. Send this missing submission now.", Some("Late")),
          TimelineEvent(s"Quarter: $testDateTwo to $testDateTwo", s"Due on $testDateTwo. Send this missing submission now.", Some("Late")))
        result shouldBe expected
      }
    }

    "return an empty sequence" when {
      "there are no open events" in {
        val result = service.buildTimeline(compliancePayloadObligationsFulfilled)(messages)
        val expected = Seq()
        result shouldBe expected
      }
    }
  }
}
