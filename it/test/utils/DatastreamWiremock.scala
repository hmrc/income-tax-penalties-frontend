package utils

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.Status

trait DatastreamWiremock {
  def mockMergedAuditResponse(): StubMapping = {
    stubFor(post(urlPathEqualTo(s"/write/audit/merged"))
      .willReturn(
        aResponse()
          .withStatus(Status.NO_CONTENT)))
  }

  def mockAuditResponse(): StubMapping = {
    stubFor(post(urlPathEqualTo(s"/write/audit"))
      .willReturn(
        aResponse()
          .withStatus(Status.NO_CONTENT)))
  }
}
