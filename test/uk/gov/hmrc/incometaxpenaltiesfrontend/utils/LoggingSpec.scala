/*
 * Copyright 2026 HM Revenue & Customs
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

package uk.gov.hmrc.incometaxpenaltiesfrontend.utils

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.play.bootstrap.tools.LogCapturing

class LoggingSpec extends AnyWordSpec with Matchers with LogCapturing {

  class FirstLoggingClass extends Logging {
    def logSomething(): Unit = logger.info("hello from first class")

    // expose for assertions - the real `logger` field is protected
    def testLogger: play.api.Logger = logger
  }

  class SecondLoggingClass extends Logging {
    def logSomething(): Unit = logger.info("hello from second class")

    def testLogger: play.api.Logger = logger
  }

  "Logging" should {

    "give each class mixing it in its own uniquely named logger, rather than one shared/universal logger" in {
      val first = new FirstLoggingClass
      val second = new SecondLoggingClass

      first.testLogger.logger.getName should include("FirstLoggingClass")
      second.testLogger.logger.getName should include("SecondLoggingClass")

      // Critically, the two loggers must NOT share the same (old universal) name
      first.testLogger.logger.getName should not be second.testLogger.logger.getName
    }

    "only capture log lines emitted by the specific class under test" in {
      val first = new FirstLoggingClass
      val second = new SecondLoggingClass

      withCaptureOfLoggingFrom(first.testLogger) {
        logs =>
          first.logSomething()
          second.logSomething()

          logs.exists(_.getMessage.contains("hello from first class")) shouldBe true
          logs.exists(_.getMessage.contains("hello from second class")) shouldBe false
      }
    }
  }
}

