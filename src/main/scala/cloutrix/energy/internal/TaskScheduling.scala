package cloutrix.energy.internal

import java.util.concurrent.ScheduledExecutorService
import scala.concurrent.duration.FiniteDuration

trait TaskScheduling {
  def start(interval: FiniteDuration)(implicit es: ScheduledExecutorService): Unit
  def stop(): Unit
  def awaitTermination(): Unit = ()
}

object TaskScheduling {
  def findAll(objs: Any*): Seq[TaskScheduling] = objs.collect { case s: TaskScheduling => s }
}
