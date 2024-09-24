package utils

import java.util.concurrent.atomic.AtomicReference
import scala.concurrent.Future.successful
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.{Failure, Success, Try}

object FutureUtils {
  private type Functor[T] = Option[T] | Try[T]

  case class FuturePartition[T](head: T, tail: Seq[Future[T]])

  extension [T](fp: Future[FuturePartition[T]]) def one(test: T=>Boolean)(using ExecutionContext): Future[T] = {
    fp map { partition =>
      if test(partition.head) then successful(partition.head) else partition.tail.partition().one(test)
    }
  }.flatten

  extension [T](fp: Future[FuturePartition[Functor[T]]]) def one()(using ExecutionContext): Future[T] = {
    fp map {
      case FuturePartition(Some(completed), _) => successful(completed)
      case FuturePartition(Success(completed), _) => successful(completed)
      case FuturePartition(None, remaining: Seq[Future[Functor[T]]]) => remaining.partition().one()
      case FuturePartition(Failure(_), remaining: Seq[Future[Functor[T]]]) => remaining.partition().one()
    }
  }.flatten

  /**
   * partition a sequence of futures into the first completed one (head), and the rest (tail)
   */
  extension [T](futures: Seq[Future[T]]) def partition()(using ExecutionContext): Future[FuturePartition[T]] = {
    val i = futures.iterator
    if (!i.hasNext) Future.never
    else {
      val p = Promise[FuturePartition[T]]()
      object firstCompleteHandler extends AtomicReference[Promise[FuturePartition[T]]](p) /*with (Try[Future[T]] => Unit)*/ {
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
