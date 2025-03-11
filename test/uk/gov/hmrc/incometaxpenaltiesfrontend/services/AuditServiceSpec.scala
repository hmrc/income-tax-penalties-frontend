/*
 * Copyright 2025 HM Revenue & Customs
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

import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{times, verify}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.audit.AuditModel
import uk.gov.hmrc.play.audit.http.connector.AuditConnector

import scala.concurrent.ExecutionContext

class AuditServiceSpec extends AnyWordSpec with Matchers with MockitoSugar {

  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val ec: ExecutionContext = ExecutionContext.global

  val auditConnector: AuditConnector = mock[AuditConnector]
  val service = new AuditService(auditConnector)

  val testAuditModel: AuditModel = new AuditModel {
    override val auditType: String = "auditType"
    override val detail: JsValue = Json.obj("key" -> "value")
  }

  "AuditService" should {

    "audit an explicit event" in {

      service.audit(testAuditModel)

      verify(auditConnector, times(1)).sendExplicitAudit(
        auditType = eqTo(testAuditModel.auditType),
        detail = eqTo(testAuditModel.detail)
      )(any(), any(), any())
    }
  }
}
