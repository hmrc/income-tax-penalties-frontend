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

package uk.gov.hmrc.incometaxpenaltiesfrontend.utils

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class CurrencyFormatterSpec extends AnyWordSpec with Matchers {

  "CurrencyFormatter" should {

    "format 0.00 as 0" in {
      CurrencyFormatter.parseBigDecimalNoPaddedZeroToFriendlyValue(0.00) shouldBe "0"
    }

    "format 100.00 as 100" in {
      CurrencyFormatter.parseBigDecimalNoPaddedZeroToFriendlyValue(100.00) shouldBe "100"
    }

    "format 99.99 as 99.99" in {
      CurrencyFormatter.parseBigDecimalNoPaddedZeroToFriendlyValue(99.99) shouldBe "99.99"
    }

    "format 9999.99 as 9,999.99" in {
      CurrencyFormatter.parseBigDecimalNoPaddedZeroToFriendlyValue(9999.99) shouldBe "9,999.99"
    }

    "format 9999.1 as 9,999.10" in {
      CurrencyFormatter.parseBigDecimalNoPaddedZeroToFriendlyValue(9999.1) shouldBe "9,999.10"
    }

    "format 123456789.1 as 123,456,789.10" in {
      CurrencyFormatter.parseBigDecimalNoPaddedZeroToFriendlyValue(123456789.1) shouldBe "123,456,789.10"
    }
  }
}
