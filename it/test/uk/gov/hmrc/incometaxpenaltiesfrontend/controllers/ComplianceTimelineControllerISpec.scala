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

package uk.gov.hmrc.incometaxpenaltiesfrontend.controllers

import fixtures.{ComplianceDataTestData, PenaltiesFixture}
import org.jsoup.Jsoup
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.incometaxpenaltiesfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesfrontend.featureswitch.core.config.{FeatureSwitching, UseStubForBackend}
import uk.gov.hmrc.incometaxpenaltiesfrontend.stubs.ComplianceStub

class ComplianceTimelineControllerISpec extends ControllerISpecHelper
  with PenaltiesFixture with ComplianceStub with FeatureSwitching with ComplianceDataTestData {

  override val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  val serviceName = "Manage your Self Assessment"
  val moreInformationLink = "More information about how HMRC removes penalty points (opens in new tab)"
  val returnToSALink = "Return to Self Assessment penalties and appeals (opens in new tab)"
  val pointsToBeRemoved = s"Points to be removed: December 2024"
  val taxYear1 = "Tax return: 6 April 2021 to 5 April 2022"
  val quarter1 = "Update Period: 1 July 2022 to 30 September 2022"
  val timeline1 =  "Late Due on 31 January 2023. Send this missing submission now."
  val timeline2 =  "Late Due on 31 October 2022. Send this missing submission now."

  override def beforeEach(): Unit = {
    super.beforeEach()
    disable(UseStubForBackend)
  }

  "GET /actions-to-get-points-removed" should {

    testNavBar("/actions-to-get-points-removed") {
      stubGetComplianceData(testNino, testFromDate, testPoCAchievementDate)(OK, Json.toJson(sampleCompliancePayload))
    }

    "return an OK with an individual view" when {
      "the page has the correct elements for one entry in the compliance timeline" in {
        stubAuthRequests(false)

        stubGetComplianceData(testNino, testFromDate, testPoCAchievementDate)(OK, Json.toJson(sampleCompliancePayload))

        lazy val result = get("/actions-to-get-points-removed")
        result.status shouldBe OK

        val document = Jsoup.parse(result.body)

        document.getServiceName.text() shouldBe serviceName
        document.title() shouldBe "Actions to take to get your points removed - Manage your Self Assessment - GOV.UK"
        document.getH1Elements.text() shouldBe "Actions to take to get your points removed"
        document.getParagraphs.get(0).text() shouldBe "You have the maximum number of late submission penalty points. This means that your points can no longer expire."
        document.getParagraphs.get(1).text() shouldBe "To get your points removed by HMRC, you need to send any submissions listed on this timeline before the deadline."
        document.getElementById("pointsToBeRemovedPara").text() shouldBe pointsToBeRemoved
        document.getElementById("missedDeadlinePara").text() shouldBe "If you miss a deadline, you will have to send 4 more submissions on time before HMRC can remove your points."
        document.getLink("moreInformationLink").text() shouldBe moreInformationLink
        document.getLink("returnToSA").text() shouldBe returnToSALink
        document.getH2Elements.get(0).text() shouldBe taxYear1
        document.getElementsByClass("hmrc-timeline__event-content").get(0).text() shouldBe timeline1
      }

      "the page has the correct elements for two entries in the compliance timeline " in {
        stubAuthRequests(false)

        stubGetComplianceData(testNino, testFromDate, testPoCAchievementDate)(OK, Json.toJson(sampleCompliancePayloadTwoOpen))

        lazy val result = get("/actions-to-get-points-removed")
        result.status shouldBe OK

        val document = Jsoup.parse(result.body)

        document.getServiceName.text() shouldBe serviceName
        document.title() shouldBe "Actions to take to get your points removed - Manage your Self Assessment - GOV.UK"
        document.getH1Elements.text() shouldBe "Actions to take to get your points removed"
        document.getParagraphs.get(0).text() shouldBe "You have the maximum number of late submission penalty points. This means that your points can no longer expire."
        document.getParagraphs.get(1).text() shouldBe "To get your points removed by HMRC, you need to send any submissions listed on this timeline before the deadline."
        document.getElementById("pointsToBeRemovedPara").text() shouldBe pointsToBeRemoved
        document.getElementById("missedDeadlinePara").text() shouldBe "If you miss a deadline, you will have to send 4 more submissions on time before HMRC can remove your points."
        document.getLink("moreInformationLink").text() shouldBe moreInformationLink
        document.getLink("returnToSA").text() shouldBe returnToSALink
        document.getH2Elements.get(0).text() shouldBe taxYear1
        document.getH2Elements.get(1).text() shouldBe quarter1
        document.getElementsByClass("hmrc-timeline__event-content").get(0).text() shouldBe timeline1
        document.getElementsByClass("hmrc-timeline__event-content").get(1).text() shouldBe timeline2
      }
    }

    "throw an exception" when {
      "there is no date in session" in {
        stubAuthRequests(false)

        lazy val result = getNoDateInSession("/actions-to-get-points-removed")

        result.status shouldBe INTERNAL_SERVER_ERROR
      }
    }
  }

  "GET /agent-actions-to-get-points-removed" should {

    "return an OK with an agent view" when {
      "the page has the correct elements for one entry in the compliance timeline" in {

        stubAuthRequests(true)

        stubGetComplianceData(testNino, testFromDate, testPoCAchievementDate)(OK, Json.toJson(sampleCompliancePayload))

        lazy val result = get("/agent-actions-to-get-points-removed", isAgent = true)
        result.status shouldBe OK

        val document = Jsoup.parse(result.body)

        document.getServiceName.text() shouldBe serviceName
        document.title() shouldBe "Actions your client must take to get their points removed - Manage your Self Assessment - GOV.UK"
        document.getH1Elements.text() shouldBe "Actions your client must take to get their points removed"
        document.getParagraphs.get(0).text() shouldBe "Your client has the maximum number of late submission penalty points. This means that their points can no longer expire."
        document.getParagraphs.get(1).text() shouldBe "To get their points removed by HMRC, they will need to send any submissions listed on this timeline before the deadline."
        document.getElementById("pointsToBeRemovedPara").text() shouldBe pointsToBeRemoved
        document.getElementById("missedDeadlinePara").text() shouldBe "If your client misses a deadline, they will have to send 4 more submissions on time before HMRC can remove their points."
        document.getLink("moreInformationLink").text() shouldBe moreInformationLink
        document.getLink("returnToSA").text() shouldBe returnToSALink
        document.getH2Elements.get(0).text() shouldBe taxYear1
        document.getElementsByClass("hmrc-timeline__event-content").get(0).text() shouldBe timeline1
      }

      "the page has the correct elements for two entries in the compliance timeline" in {

        stubAuthRequests(true)

        stubGetComplianceData(testNino, testFromDate, testPoCAchievementDate)(OK, Json.toJson(sampleCompliancePayloadTwoOpen))

        lazy val result = get("/agent-actions-to-get-points-removed", isAgent = true)
        result.status shouldBe OK

        val document = Jsoup.parse(result.body)

        document.getServiceName.text() shouldBe serviceName
        document.title() shouldBe "Actions your client must take to get their points removed - Manage your Self Assessment - GOV.UK"
        document.getH1Elements.text() shouldBe "Actions your client must take to get their points removed"
        document.getParagraphs.get(0).text() shouldBe "Your client has the maximum number of late submission penalty points. This means that their points can no longer expire."
        document.getParagraphs.get(1).text() shouldBe "To get their points removed by HMRC, they will need to send any submissions listed on this timeline before the deadline."
        document.getElementById("pointsToBeRemovedPara").text() shouldBe pointsToBeRemoved
        document.getElementById("missedDeadlinePara").text() shouldBe "If your client misses a deadline, they will have to send 4 more submissions on time before HMRC can remove their points."
        document.getLink("moreInformationLink").text() shouldBe moreInformationLink
        document.getLink("returnToSA").text() shouldBe returnToSALink
        document.getH2Elements.get(0).text() shouldBe taxYear1
        document.getH2Elements.get(1).text() shouldBe quarter1
        document.getElementsByClass("hmrc-timeline__event-content").get(0).text() shouldBe timeline1
        document.getElementsByClass("hmrc-timeline__event-content").get(1).text() shouldBe timeline2
      }

    }

    "throw an exception" when {
      "there is no date in session" in {
        stubAuthRequests(false)

        lazy val result = getNoDateInSession("/actions-to-get-points-removed")

        result.status shouldBe INTERNAL_SERVER_ERROR
      }
    }
  }
}
