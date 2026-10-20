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

package uk.gov.hmrc.incometaxpenaltiesfrontend.utils

/**
 * Mix this trait into a class/trait that needs to log, rather than importing a single
 * universal `Logger` instance. Each class gets its own [[play.api.Logger]] named after
 * the class it is mixed into, which makes it far easier to trace where a log line came
 * from and to tune log levels per-class if required.
 *
 * IMPORTANT: Never log Personal Identifiable Information (PII) - e.g. NINO, UTR, name,
 * email, address etc. Only log identifiers that are safe for support teams to see
 * (e.g. exception types, correlation ids, feature switch names).
 */
trait Logging {

  protected lazy val logger: play.api.Logger = play.api.Logger(getClass)

}
