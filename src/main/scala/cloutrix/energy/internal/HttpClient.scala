package cloutrix.energy.internal

import com.typesafe.scalalogging.LazyLogging
import scalaj.http.{Http, HttpOptions}

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
                       tls: Boolean = false,
                       authToken: Option[String] = None
                     )

object HttpClient {
  final val DefaultConnectionTimeout: Duration = 2.seconds
  final val DefaultReadTimeout: Duration = 5.seconds
}

trait HttpClient extends LazyLogging {
  protected def doHttpRequest[T](path: String, reader: String => T)(implicit config: HttpConfig): T = {
    def urlFor(path: String) = Some(new URL(if (config.tls) "https" else "http", config.host, config.port, path))
      .tapEach(url => logger.debug(s"HTTP URL: ${url.toString}"))
      .head

    def reqFor(url: URL) = {
      def plainRequest = Http(url.toString)
        .timeout(config.connectionTimeout.toMillis.toInt, config.readTimeout.toMillis.toInt)
        .options(HttpOptions.allowUnsafeSSL)

      val req = config.authToken
        .map("Bearer %s".format(_))
        .map(plainRequest.header("Authorization", _))
        .getOrElse(
          plainRequest
        )

      logger.debug(s"HTTP REQUEST: ${req.toString}")
      req
    }

    reader(reqFor(urlFor(path)).asString.body)
  }
}
