package helpers

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig

trait WiremockHelper {

  val stubPort = 9999
  val stubHost = "localhost"

  var wireMockServer: WireMockServer = new WireMockServer(wireMockConfig().port(stubPort))

  def host(): String = s"http://$stubHost:$stubPort"

  def startWiremock(): Unit = {
    if (!wireMockServer.isRunning) {
      wireMockServer.start()
      WireMock.configureFor(stubHost, stubPort)
    }
  }

  def stopWiremock(): Unit = {
    wireMockServer.stop()
  }

  def resetAll(): Unit = {
    wireMockServer.resetMappings()
    wireMockServer.resetRequests()
    wireMockServer.resetAll()
  }
}