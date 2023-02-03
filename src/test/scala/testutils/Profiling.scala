package testutils

import scala.annotation.tailrec
import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.language.postfixOps
import scala.util.chaining.scalaUtilChainingOps

trait Profiling {
  def Loop[T](duration: FiniteDuration, interval: Duration = Duration.Zero)(block: => T): Long = {
    val start = System.currentTimeMillis()

    @tailrec def run(count: Long = 0L): Long = {
      if(System.currentTimeMillis() - start >= duration.toMillis) return count

      val rT = block
      if(interval.gt(Duration.Zero)) Thread.sleep(interval.toMillis)
      run(count + 1)
    }

    run()
  }

  def Time[T](block: => T): FiniteDuration = {
    Some(System.nanoTime())
      .tap(_ => block)
      .map(System.nanoTime() - _)
      .map(Duration.fromNanos)
      .get
  }
}
