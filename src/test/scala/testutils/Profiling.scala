package testutils

import scala.annotation.tailrec
import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.language.postfixOps
import scala.util.chaining.scalaUtilChainingOps

trait Profiling {
    def Loop[T](duration: FiniteDuration, interval: Duration = Duration.Zero)(block: => T): (Long, T) = {
        val start = System.currentTimeMillis()

        @tailrec def run(count: Long = 1L, result: T): (Long, T) = {
            if(System.currentTimeMillis() - start >= duration.toMillis) return (count, result)
            if(interval.gt(Duration.Zero)) Thread.sleep(interval.toMillis)
            run(count + 1, block)
        }

        run(result = block)
    }

    def Time[T](block: => T): FiniteDuration = {
        Some(System.nanoTime())
            .tap(_ => block)
            .map(System.nanoTime() - _)
            .map(Duration.fromNanos)
            .get
    }
}
