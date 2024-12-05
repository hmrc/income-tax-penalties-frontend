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

import org.jsoup.Jsoup
import play.api.http.Status.OK
import uk.gov.hmrc.incometaxpenaltiesfrontend.stubs.AuthStub
import uk.gov.hmrc.incometaxpenaltiesfrontend.utils.{ComponentSpecHelper, ViewSpecHelper}

class ComplianceTimelineControllerISpec extends ComponentSpecHelper with ViewSpecHelper with AuthStub {

  val serviceName = "Manage your Self Assessment"
  val moreInformationLink = "More information about how HMRC removes penalty points (opens in a new tab) (opens in new tab)"
  val returnToSALink = "Return to Self Assessment penalties and appeals (opens in new tab)"
  val pointsToBeRemoved = "Points to be removed: March 2029"
  val quarter1 = "Quarter: 6 July 2027 to 5 October 2027"
  val quarter2 = "Quarter: 6 October 2027 to 5 January 2028"
  val quarter3 = "Quarter: 6 January 2028 to 5 April 2028"
  val quarter4 = "Quarter: 6 April 2028 to 5 July 2028"
  val quarter5 = "Quarter: 6 July 2028 to 5 October 2028"
  val taxReturn = "Tax return: 2027 to 2028"
  val quarter6 = "Quarter: 6 October 2028 to 5 January 2029"
  val timeline1 =  "Late Due on 5 November 2027. Send this missing submission now."
  val timeline2 =  "Late Due on 5 February 2028. Send this missing submission now."
  val timeline3 =  "Send by 5 May 2028"
  val timeline4 =  "Send by 5 August 2028"
  val timeline5 =  "Send by 5 November 2028"
  val timeline6 =  "Send by 31 January 2029"
  val timeline7 =  "Send by 5 February 2029"




  "GET /compliance-timeline" should {
    "return an OK with an individual view" when {



      "have the correct page has correct elements" in {
        stubAuth(OK, successfulIndividualAuthResponse)
        lazy val result = get("/compliance-timeline")
        result.status shouldBe OK

        val document = Jsoup.parse(result.body)


        document.getServiceName.text() shouldBe serviceName
        document.title() shouldBe "Actions to take to get your points removed - Manage your Self Assessment - GOV.UK"
        document.getH1Elements.text() shouldBe "Actions to take to get your points removed"
        document.getParagraphs.get(1).text() shouldBe "You have the maximum number of late submission penalty points. This means that your points can no longer expire."
        document.getParagraphs.get(2).text() shouldBe "To get your points removed by HMRC, you need to send any submissions listed on this timeline before the deadline."
        document.getElementById("pointsToBeRemovedPara").text() shouldBe pointsToBeRemoved
        document.getElementById("missedDeadlinePara").text() shouldBe "If you miss a deadline, you will have to send 4 more submissions on time before HMRC can remove your points."
        document.getLink("moreInformationLink").text() shouldBe moreInformationLink
        document.getLink("returnToSA").text() shouldBe returnToSALink
        document.getH2Elements.get(0).text() shouldBe quarter1
        document.getH2Elements.get(1).text() shouldBe quarter2
        document.getH2Elements.get(2).text() shouldBe quarter3
        document.getH2Elements.get(3).text() shouldBe quarter4
        document.getH2Elements.get(4).text() shouldBe quarter5
        document.getH2Elements.get(5).text() shouldBe taxReturn
        document.getH2Elements.get(6).text() shouldBe quarter6
        document.getElementsByClass("hmrc-timeline__event-content").get(0).text() shouldBe timeline1
        document.getElementsByClass("hmrc-timeline__event-content").get(1).text() shouldBe timeline2
        document.getElementsByClass("hmrc-timeline__event-content").get(2).text() shouldBe timeline3
        document.getElementsByClass("hmrc-timeline__event-content").get(3).text() shouldBe timeline4
        document.getElementsByClass("hmrc-timeline__event-content").get(4).text() shouldBe timeline5
        document.getElementsByClass("hmrc-timeline__event-content").get(5).text() shouldBe timeline6
        document.getElementsByClass("hmrc-timeline__event-content").get(6).text() shouldBe timeline7


      }
    }


    "return an OK with an agent view" when {

      "have the correct page has correct elements" in {

        stubAuth(OK, successfulAgentAuthResponse)
        lazy val result = get("/compliance-timeline", isAgent = true)
        result.status shouldBe OK

        val document = Jsoup.parse(result.body)

        document.getServiceName.text() shouldBe serviceName
        document.title() shouldBe "Actions your client must take to get their points removed - Manage your Self Assessment - GOV.UK"
        document.getH1Elements.text() shouldBe "Actions your client must take to get their points removed"
        document.getParagraphs.get(1).text() shouldBe "Your client has the maximum number of late submission penalty points. This means that their points can no longer expire."
        document.getParagraphs.get(2).text() shouldBe "To get their points removed by HMRC, they will need to send any submissions listed on this timeline before the deadline."
        document.getElementById("pointsToBeRemovedPara").text() shouldBe pointsToBeRemoved
        document.getElementById("missedDeadlinePara").text() shouldBe "If your client misses a deadline, they will have to send 4 more submissions on time before HMRC can remove their points."
        document.getLink("moreInformationLink").text() shouldBe moreInformationLink
        document.getLink("returnToSA").text() shouldBe returnToSALink
        document.getH2Elements.get(0).text() shouldBe quarter1
        document.getH2Elements.get(1).text() shouldBe quarter2
        document.getH2Elements.get(2).text() shouldBe quarter3
        document.getH2Elements.get(3).text() shouldBe quarter4
        document.getH2Elements.get(4).text() shouldBe quarter5
        document.getH2Elements.get(5).text() shouldBe taxReturn
        document.getH2Elements.get(6).text() shouldBe quarter6
        document.getElementsByClass("hmrc-timeline__event-content").get(0).text() shouldBe timeline1
        document.getElementsByClass("hmrc-timeline__event-content").get(1).text() shouldBe timeline2
        document.getElementsByClass("hmrc-timeline__event-content").get(2).text() shouldBe timeline3
        document.getElementsByClass("hmrc-timeline__event-content").get(3).text() shouldBe timeline4
        document.getElementsByClass("hmrc-timeline__event-content").get(4).text() shouldBe timeline5
        document.getElementsByClass("hmrc-timeline__event-content").get(5).text() shouldBe timeline6
        document.getElementsByClass("hmrc-timeline__event-content").get(6).text() shouldBe timeline7


      }
    }
  }
}
