package utils

import com.codahale.metrics.SharedMetricRegistries
import helpers.WiremockHelper
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, TestSuite}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.inject.Injector
import uk.gov.hmrc.http.HeaderCarrier

trait IntegrationSpecCommonBase extends AnyWordSpec with Matchers with GuiceOneServerPerSuite with BeforeAndAfterAll with BeforeAndAfterEach with WiremockHelper with TestSuite with DatastreamWiremock {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  lazy val injector: Injector = app.injector

  override def afterEach(): Unit = {
    resetAll()
    super.afterEach()
    SharedMetricRegistries.clear()
  }

  override def beforeEach(): Unit = {
    super.beforeEach()
    mockAuditResponse()
    mockMergedAuditResponse()
    SharedMetricRegistries.clear()
  }

  override def beforeAll(): Unit = {
    super.beforeAll()
    startWiremock()
    SharedMetricRegistries.clear()
  }

  override def afterAll(): Unit = {
    super.afterAll()
    resetAll()
    stopWiremock()
    SharedMetricRegistries.clear()
  }

}
