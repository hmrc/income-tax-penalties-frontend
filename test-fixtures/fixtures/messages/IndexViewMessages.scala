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

object IndexViewMessages {

  sealed trait Messages { _: i18n =>
    val noLSP = "There are no late submission penalties."
    val noLPP = "There are no late payment penalties."
  }

  object English extends Messages with En

  object Welsh extends Messages with Cy {
    override val noLSP = "There are no late submission penalties. (Welsh)"
    override val noLPP = "There are no late payment penalties. (Welsh)"
  }
}
