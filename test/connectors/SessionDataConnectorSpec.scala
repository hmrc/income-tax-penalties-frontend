package connectors

import config.AppConfig
import connectors.SessionDataConnector.SessionData
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.{mock, times, verify, when}
import org.scalatest.Assertion
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, SessionId}
import utils.ServiceEndpoint

import scala.concurrent.Future.{failed, successful}
import scala.concurrent.{ExecutionContext, Future}

/**
 * TODO: this needs to cover more of the responses the session data service can respond with
 */
class SessionDataConnectorSpec extends AsyncWordSpec with Matchers /*with GuiceOneAppPerSuite*/ {

  given HeaderCarrier(sessionId = Some(SessionId("xyz")))

  val mockHttpV2 = mock(classOf[HttpClientV2], Mockito.RETURNS_DEEP_STUBS)
  val mockConfig = mock(classOf[AppConfig])

  when(mockConfig.sessionDataService).thenReturn(ServiceEndpoint(protocol = "http", host = "foo", port = None, prefix = "bar"))

  val connector = new SessionDataConnector(mockHttpV2, mockConfig)(ExecutionContext.global)

  "getSessionData" should {
    "return empty session data if no session data held" in {
      when(mockHttpV2.get(any())(any()).execute[SessionData](any(), any())).thenReturn(Future.failed(uk.gov.hmrc.http.NotFoundException("y'all")))

      val futureData = connector.getSessionData;

      futureData.map { data =>
        data shouldBe SessionData(sessionId = Some("xyz"))
      }
    }

    "return session data if session data held" in {
      val sessionData = SessionData(mtditid = Some("anMtdItId"), nino = Some("aNino"), sessionId = Some("xyz"))
      when(mockHttpV2.get(any())(any()).execute[SessionData](any(), any())).thenReturn(successful(sessionData))

      val futureData = connector.getSessionData;

      futureData.map { data =>
        data shouldBe sessionData
      }
    }
  }

  "setSessionData" should {
    "post the provided session data to the session service" in {
      val sessionData = SessionData(mtditid = Some("anMtdItId"), nino = Some("aNino"), sessionId = Some("xyz"))
      when(mockHttpV2
        .post(any())(any())
        .withBody(any())(any(), any(), any())
        .execute[SessionData](any(), any())
      ).thenReturn(successful(uk.gov.hmrc.http.HttpResponse(200)))

      val futureData = connector.putSessionData(sessionData);

      futureData.map { _ =>
        verify(
          mockHttpV2, times(2)).post(any())(any())
        1 shouldBe 1
      }
    }
  }
}
