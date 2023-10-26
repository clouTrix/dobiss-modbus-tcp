package cloutrix.energy.internal

import com.typesafe.scalalogging.LazyLogging
import scalaj.http.{Http, HttpOptions, HttpRequest, HttpResponse}

import java.net.URL
import scala.Option.option2Iterable
import scala.collection.IterableOnce.iterableOnceExtensionMethods
import scala.concurrent.duration.{Duration, DurationInt}
import scala.util.chaining.scalaUtilChainingOps

case class HttpConfig(
                       host: String,
                       port: Int,
                       connectionTimeout: Duration = HttpClient.DefaultConnectionTimeout,
                       readTimeout: Duration = HttpClient.DefaultReadTimeout,
                       tls: Boolean = false
                     )

object HttpClient {
  final val DefaultConnectionTimeout: Duration = 10.seconds
  final val DefaultReadTimeout: Duration = 10.seconds
}

trait HttpClient extends LazyLogging {
  protected def on401(): Unit = {}

  protected def addCustomHeaders(req: HttpRequest): HttpRequest = req

  def urlFor(path: String)(implicit config: HttpConfig): URL = Some(new URL(if (config.tls) "https" else "http", config.host, config.port, path))
    .tapEach(url => logger.debug(s"HTTP URL: ${url.toString}"))
    .head

  def reqFor(url: URL)(implicit config: HttpConfig): HttpRequest = Http(url.toString)
    .timeout(config.connectionTimeout.toMillis.toInt, config.readTimeout.toMillis.toInt)
    .options(HttpOptions.allowUnsafeSSL)

  protected def doHttpRequest[T](path: String, reader: String => T)(implicit config: HttpConfig): Option[T] = {
    Some(reqFor(urlFor(path)))
      .tapEach(req => logger.debug(s"HTTP REQUEST: ${req.toString}"))
      .map(addCustomHeaders(_).asString)
      .flatMap {
        case HttpResponse(_, 401, _) => on401(); None
        case resp if resp.is2xx => Some(reader(resp.body))
        case resp =>
          logger.error(s"error executing HTTP request - response-code: ${resp.code}")
          None
      }.headOption
  }
}
