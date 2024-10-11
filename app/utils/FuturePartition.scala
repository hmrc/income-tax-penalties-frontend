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

import utils.FuturePartition.race

import java.util.concurrent.atomic.AtomicReference
import scala.+:
import scala.concurrent.Future.successful
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.{Success, Try}

/**
 * Partitioned futures into the race winner and means to obtain the rest.
 */
class FuturePartition[T] (val head: Try[T], private[utils] val tail: Seq[Future[T]], private val test: T=>Boolean = (_:T)=>true) {
  /** 
   * @return the partitioned tail of this partition 
   * */
  def next(using ExecutionContext) = race(tail*).matching(test)
}

/**
 * Partition a sequence of futures into a race winner and the rest, and
 * provides wethods for selecting and traversing the race results.
 *
 * For futures in general, to get the first 5 successful results:
 *   race(futures).matching(_).take(5)
 *
 * Also for future options, to get the first successful non-empty result:
 *   race(futures).firstNonEmpty
 */
object FuturePartition {
  extension [T](fp: Future[FuturePartition[T]]) {
    /**
     * @return a partition containing only successful results that match the specified predicate test
     */
    def matching(test: T => Boolean)(using ExecutionContext): Future[FuturePartition[T]] = {
      fp map { partition =>
        partition.head match {
          case Success(value) if test(value) => successful(FuturePartition(partition.head, partition.tail, test))
          case _ => race(partition.tail*).matching(test)
        }
      }
    }.flatten

    /** 
     * @return the first n successful results
     * @throws Exception if a failure is encountered
     */
    def take(n: Int)(using ExecutionContext): Future[Seq[T]] = {
      fp flatMap { partition =>
        n match {
          case 0 => successful(Seq.empty[T])
          case 1 => successful(Seq(partition.head.get))
          case n => partition.next.take(n - 1) map { partition.head.get +: _ }
        }
      }
    }

    /**
     * @return the first successful result
     * @throws Exception if a failure is encountered
     */
    def first(using ExecutionContext): Future[T] = fp map (_.head.get)
  }

  extension [T](fp: Future[FuturePartition[Option[T]]]) {
    /**
     * @return a partition containing only successful results that are non-empty
     */
    def nonEmpty(using ExecutionContext): Future[FuturePartition[Option[T]]] = fp.matching(_.isDefined)
    /**
     * @return the first successful non-empty result
     */
    def firstNonEmpty(using ExecutionContext): Future[T] = nonEmpty.first.map(_.get)
  }

  /**
   * Partitions the futures provided
   */
  def race[T](futures: Future[T]*)(using ExecutionContext): Future[FuturePartition[T]] = {
    if (futures.isEmpty) {
      throw new NoSuchElementException()
    } else {
      val promise = Promise[FuturePartition[T]]()
      val oneShotRef = AtomicReference[Promise[FuturePartition[T]]](promise)
      futures.foreach { future =>
        future.onComplete { result =>
          Option(oneShotRef.getAndSet(null)) foreach { promise =>
            promise tryComplete Try(FuturePartition(result, futures.filterNot(_==future)))
          }
        }
      }
      promise.future
    }
  }
}