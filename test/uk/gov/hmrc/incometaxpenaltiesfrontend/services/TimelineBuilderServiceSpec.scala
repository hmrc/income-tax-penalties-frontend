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
import fixtures.messages.ComplianceTimelineMessages
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Lang, Messages, MessagesApi}
import uk.gov.hmrc.incometaxpenaltiesfrontend.utils.{DateFormatter, TimeMachine}
import uk.gov.hmrc.incometaxpenaltiesfrontend.viewModels.TimelineEvent

import java.time.LocalDate

class TimelineBuilderServiceSpec extends AnyWordSpec with Matchers with ComplianceDataTestData with GuiceOneAppPerSuite with DateFormatter {

  class Setup(runDate: LocalDate = LocalDate.of(2023,4,13)) {
    object FakeTimeMachine extends TimeMachine {
      override def getCurrentDate: LocalDate = runDate
    }
    val service: TimelineBuilderService = new TimelineBuilderService(FakeTimeMachine)
  }

  Seq(ComplianceTimelineMessages.English, ComplianceTimelineMessages.Welsh).foreach { messagesForLang =>

    s"rendering in language of '${messagesForLang.lang.name}'" when {

      implicit lazy val messages: Messages = app.injector.instanceOf[MessagesApi].preferred(Seq(Lang(messagesForLang.lang.code)))

      "buildTimeline" should {
        "return a sequence of timeline events " when {
          "there is one open event (late)" in new Setup() {
            val result = service.buildTimeline(Some(sampleCompliancePayload))
            val expected = Seq(
              TimelineEvent(
                headerContent = messagesForLang.taxReturn(LocalDate.of(2021, 4, 6), LocalDate.of(2022, 4, 5)),
                spanContent = messagesForLang.dueDate(LocalDate.of(2023, 1, 31), isLate = true),
                tagContent = Some(messagesForLang.late),
              )
            )
            result shouldBe expected
          }

          "there are two open events (both late)" in new Setup() {
            val result = service.buildTimeline(Some(sampleCompliancePayloadTwoOpen))
            val expected = Seq(
              TimelineEvent(
                headerContent = messagesForLang.taxReturn(LocalDate.of(2021, 4, 6), LocalDate.of(2022, 4, 5)),
                spanContent = messagesForLang.dueDate(LocalDate.of(2023, 1, 31), isLate = true),
                tagContent = Some(messagesForLang.late),
              ),
              TimelineEvent(
                headerContent = messagesForLang.quarter(LocalDate.of(2022, 7, 1), LocalDate.of(2022, 9, 30)),
                spanContent = messagesForLang.dueDate(LocalDate.of(2022, 10, 31), isLate = true),
                tagContent = Some(messagesForLang.late)
              )
            )
            result shouldBe expected
          }

          "there are two open events (one late)" in new Setup(LocalDate.of(2022,11,1)) {
            val result = service.buildTimeline(Some(sampleCompliancePayloadTwoOpen))
            val expected = Seq(
              TimelineEvent(
                headerContent = messagesForLang.taxReturn(LocalDate.of(2021, 4, 6), LocalDate.of(2022, 4, 5)),
                spanContent = messagesForLang.dueDate(LocalDate.of(2023, 1, 31), isLate = false),
                tagContent = None
              ),
              TimelineEvent(
                headerContent = messagesForLang.quarter(LocalDate.of(2022, 7, 1), LocalDate.of(2022, 9, 30)),
                spanContent = messagesForLang.dueDate(LocalDate.of(2022, 10, 31), isLate = true),
                tagContent = Some(messagesForLang.late)
              )
            )
            result shouldBe expected
          }

          "there are two open events (neither late)" in new Setup(LocalDate.of(2022,10,31)) {
            val result = service.buildTimeline(Some(sampleCompliancePayloadTwoOpen))
            val expected = Seq(
              TimelineEvent(
                headerContent = messagesForLang.taxReturn(LocalDate.of(2021, 4, 6), LocalDate.of(2022, 4, 5)),
                spanContent = messagesForLang.dueDate(LocalDate.of(2023, 1, 31), isLate = false),
                tagContent = None
              ),
              TimelineEvent(
                headerContent = messagesForLang.quarter(LocalDate.of(2022, 7, 1), LocalDate.of(2022, 9, 30)),
                spanContent = messagesForLang.dueDate(LocalDate.of(2022, 10, 31), isLate = false),
                tagContent = None
              )
            )
            result shouldBe expected
          }
        }

        "return an empty sequence" when {
          "there are no open events" in new Setup() {
            val result = service.buildTimeline(Some(compliancePayloadObligationsFulfilled))
            val expected = Seq()
            result shouldBe expected
          }

          "there is no Compliance Obligation data to render" in new Setup() {
            val result = service.buildTimeline(None)
            val expected = Seq()
            result shouldBe expected
          }
        }
      }
    }
  }
}
