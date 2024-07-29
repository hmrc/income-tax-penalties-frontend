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

package utils

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.Status

/**
 * @see "https://github.com/hmrc/auth/blob/main/docs/main.md"
 * @see "https://confluence.tools.tax.service.gov.uk/pages/viewpage.action?spaceKey=GG&title=Authorisation+for+microservices#Authorisationformicroservices-Callingtheauthendpointdirectly"
 * @see "https://wiremock.org/docs/request-matching/"
 */
trait AuthWiremockStubs {

  private val authRequestBody = """{
                                  |  "authorise": [
                                  |    {
                                  |      "identifiers" : [ ],
                                  |      "state" : "Activated",
                                  |      "enrolment" : "HMRC-MTD-IT"
                                  |    }
                                  |  ],
                                  |  "retrieve": ["authorisedEnrolments", "nino"]
                                  |}""".stripMargin

  def mockUnauthorisedResponse(): StubMapping = {
    stubFor(post(urlPathEqualTo(s"/auth/authorise")).withRequestBody(equalToJson(authRequestBody))
      .willReturn(aResponse()
      .withStatus(Status.NO_CONTENT)
    ))
  }

  def mockNonMtdPtResponse(nino: String = "AB123456A"): StubMapping = {
    stubFor(post(urlPathEqualTo(s"/auth/authorise")).withRequestBody(equalToJson(authRequestBody)).willReturn(aResponse()
      .withStatus(Status.OK)
      .withHeader("Content-Type", "application/json")
      .withBody(
        s"""{
           |  "authorisedEnrolments": [{
           |    "key": "HMRC-PT",
           |    "identifiers": [{ "key": "NINO", "value": "$nino" }],
           |    "state": "Activated"
           |  }],
           |  "nino": "$nino"
           |}""".stripMargin
      )
    ))
  }

  def mockEnroledResponse(mtdItId: String = "12345678901234567890", nino: String = "AB123456A"): StubMapping = {
    stubFor(post(urlPathEqualTo(s"/auth/authorise")).withRequestBody(equalToJson(authRequestBody)).willReturn(aResponse()
      .withStatus(Status.OK)
      .withHeader("Content-Type", "application/json")
      .withBody(
        s"""{
          |  "authorisedEnrolments": [{
          |    "key": "HMRC-MTD-IT",
          |    "identifiers": [{ "key": "MTDITID", "value": "$mtdItId" }],
          |    "state": "Activated"
          |  }],
          |  "nino": "$nino"
          |}""".stripMargin
      )
    ))
  }

}
