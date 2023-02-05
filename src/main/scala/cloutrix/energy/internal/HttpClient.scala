package cloutrix.energy.internal

import scalaj.http.Http

import java.net.URL
import scala.concurrent.duration.{Duration, DurationInt}

case class HttpConfig(
                       host: String,
                       port: Int,
                       connectionTimeout: Duration = HttpClient.DefaultConnectionTimeout,
                       readTimeout: Duration = HttpClient.DefaultReadTimeout
                     )

object HttpClient {
  final val DefaultConnectionTimeout: Duration = 2.seconds
  final val DefaultReadTimeout: Duration = 5.seconds
}

trait HttpClient {
  protected def doHttpRequest[T](path: String, reader: String => T)(implicit config: HttpConfig): T = {
    def urlFor(path: String) = new URL("http", config.host, config.port, path)
    def reqFor(url: URL) = Http(url.toString).timeout(config.connectionTimeout.toMillis.toInt, config.readTimeout.toMillis.toInt)

    reader(reqFor(urlFor(path)).asString.body)
  }
}
