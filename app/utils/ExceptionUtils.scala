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

package utils

import scala.concurrent.Future
import scala.concurrent.Future.failed
import scala.util.{Failure, Success, Try}

object ExceptionUtils {
  implicit class ThrowableImplicits(th: Throwable) {
    def summary: String = Option(th).map { th =>
      th.getClass.getSimpleName + Option(th.getMessage).filterNot(_.isBlank).map(_.prependedAll(": ")).getOrElse("")
    }.getOrElse("null")
  }

  implicit class FutureBodyFunctionImplicits[T](t: => Future[T]) {
    def delayFailure: Future[T] = Try(t) match {
      case Success(aFuture) => aFuture
      case Failure(th) => failed(th)
    }
  }
}
