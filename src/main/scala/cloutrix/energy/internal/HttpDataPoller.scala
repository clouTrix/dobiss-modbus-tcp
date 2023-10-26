package cloutrix.energy.internal

import com.typesafe.scalalogging.LazyLogging

import java.util.concurrent.{ScheduledExecutorService, ScheduledFuture}
import scala.concurrent.duration.FiniteDuration
import scala.util.Try
import scala.util.chaining.scalaUtilChainingOps

abstract class HttpDataPoller extends ScheduledDataPoller with HttpClient with LazyLogging {
  private var actions = Map.empty[String, (Runnable, Boolean)]
  private var tasks = Map.empty[String, ScheduledFuture[_]]

  private var scheduleAction: (String, Runnable) => (String, ScheduledFuture[_]) = (_, _) => throw new IllegalStateException("Poller not started")

  final override def start(interval: FiniteDuration)(implicit es: ScheduledExecutorService): Unit = {
    scheduleAction = (id: String, task: Runnable) => {
      logger.debug(s"schedule task - id: ${id}, interval: ${interval.toCoarsest.toString()}")
      id -> es.scheduleAtFixedRate(task, 0, interval.length, interval.unit)
    }

    tasks = actions
              .withFilter { case (_, (_, autoStart)) => autoStart }
              .map { case (id, (task, _)) =>
                 scheduleAction(id, task)
              }

    onStart()
  }

  final override def stop(): Unit = {
    tasks.keys.foreach(cancelTask)
    tasks = Map.empty
    onStop()
  }

  final protected def cancelTask(id: String): Unit = {
    tasks
      .get(id)
      .map(_.cancel(true))
      .tap(s => logger.debug(s"canceled task - id: ${id}, status: ${s.isDefined}"))
      .tapEach(_ => tasks -= id)
      .head
  }

  final protected def startTask(id: String): Boolean = {
    if (tasks.contains(id)) return false  // already scheduled

    actions
      .get(id)
      .map { case (task, _) => scheduleAction(id, task) }
      .tapEach(tasks += _)
      .nonEmpty
  }

  final protected def register[T](action: (String, (String, String => T)), autoStart: Boolean = true)(implicit httpConfig: HttpConfig): Unit = {
    val (id, (path, codec)) = action
    def task: Runnable = {
      def onDataLogged(id: String, data: Any): Unit = {
        logger.debug(s"got data - id: ${id}, path: ${path}, data: ${data}")
        onData(id, doHttpRequest(path, codec))
      }

      () => Try(onDataLogged(id, doHttpRequest(path, codec)))
              .recover { case err: Throwable => onError(err) }
    }

    actions += (id -> (task, autoStart))
  }

  protected def onStart(): Unit = {}

  protected def onStop(): Unit = {}

  protected def onError(cause: Throwable): Unit = logger.error(s"Unhandled error: ${cause.toString}", cause)

  protected def onData(id: String, data: Option[Any]): Unit
}
