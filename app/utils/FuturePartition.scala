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

import java.util.concurrent.atomic.AtomicReference
import scala.concurrent.Future.successful
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.{Failure, Success, Try}

case class FuturePartition[T](head: T, tail: Seq[Future[T]])

object FuturePartition {

  /**
   * find the first future that completes passing the specified test
   */
  extension [T](fp: Future[FuturePartition[T]]) def find(test: T=>Boolean)(using ExecutionContext): Future[T] = {
    fp map { partition =>
      if test(partition.head) then successful(partition.head) else partition.tail.partition().find(test)
    }
  }.flatten

  /**
   * find the first future that completes with some value
   */
  extension [T](fp: Future[FuturePartition[Option[T]]]) def one()(using ExecutionContext): Future[T] =
    fp.find(_.isDefined).map(_.get)

  /**
   * find the first future that completes successfully
   */
  extension [T](fp: Future[FuturePartition[Try[T]]]) def success()(using ExecutionContext): Future[T] =
    fp.find(_.isSuccess).map(_.get)

  /**
   * partition a sequence of futures into the first completed one (head), and the rest (tail)
   */
  extension [T](futures: Seq[Future[T]]) def partition()(using ExecutionContext): Future[FuturePartition[T]] = {
    val i = futures.iterator
    if (!i.hasNext) Future.never
    else {
      val p = Promise[FuturePartition[T]]()
      object firstCompleteHandler extends AtomicReference[Promise[FuturePartition[T]]](p) {
        def apply(f: Future[T], v: Try[T]): Unit = {
          val r = getAndSet(null)
          if (r ne null) {
            val remaining: Seq[Future[T]] = futures.filterNot(_==f)
            r tryComplete Try(FuturePartition(v.get, remaining))
          }
        }
      }
      while (i.hasNext && firstCompleteHandler.get != null) { // exit early if possible
        val f = i.next()
        f.onComplete(v=>firstCompleteHandler(f,v))
      }
      p.future
    }
  }

}
