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

package fixtures.messages

object ExpiryReasonMessages {

  sealed trait Messages { _: i18n =>
    val appeal = "Appeal"
    val adjustment = "Adjustment"
    val reversal = "Reversal"
    val manual = "Manual"
    val naturalExpiration = "Natural expiration"
    val submission = "Submission on time"
    val compliance = "Compliance"
    val reset = "Reset"
  }

  object English extends Messages with En

  object Welsh extends Messages with Cy {
    override val appeal = "Appeal (Welsh)"
    override val adjustment = "Adjustment (Welsh)"
    override val reversal = "Reversal (Welsh)"
    override val manual = "Manual (Welsh)"
    override val naturalExpiration = "Natural expiration (Welsh)"
    override val submission = "Submission on time (Welsh)"
  }
}
