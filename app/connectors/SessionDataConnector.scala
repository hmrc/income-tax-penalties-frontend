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

package connectors

import config.AppConfig
import play.api.Logging
import play.api.libs.json.{Format, Json}
import play.api.libs.ws.JsonBodyWritables
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, HttpResponse}

import javax.inject.Inject
import scala.concurrent.Future.successful
import scala.concurrent.{ExecutionContext, Future}

object SessionDataConnector {
  case class SessionData(
    mtditid: Option[String] = None,
    nino: Option[String] = None,
    utr: Option[String] = None,
    sessionId: Option[String]
  )

  implicit val getPenaltyDetailsFmt: Format[SessionData] = Json.format[SessionData]
}

class SessionDataConnector @Inject()(httpClient: HttpClientV2, val appConfig: AppConfig)(implicit ec: ExecutionContext) extends Logging {
  import SessionDataConnector.*

  private lazy val sessionDataServiceUrl = appConfig.sessionDataService.resolve("/") //new URL("http://localhost:30027/income-tax-session-data/")

  def getSessionData(implicit headerCarrier: HeaderCarrier): Future[SessionData] = {
    logger.info(s"headerCarrier.sessionId = ${headerCarrier.sessionId}")
    val x = httpClient.get(sessionDataServiceUrl).execute[SessionData]
    x.recoverWith {
      case _: uk.gov.hmrc.http.NotFoundException => successful(SessionData(sessionId = headerCarrier.sessionId.map(_.value)))
    }
  }

  def putSessionData(sessionData: SessionData)(using headerCarrier: HeaderCarrier): Future[Unit] = {
    import JsonBodyWritables.writeableOf_JsValue
    httpClient.post(sessionDataServiceUrl).withBody(Json.toJson(sessionData)).execute[HttpResponse] map { response =>
      ()
    }
  }
}