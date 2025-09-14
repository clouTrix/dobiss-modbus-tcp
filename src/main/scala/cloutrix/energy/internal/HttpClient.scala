package cloutrix.energy.internal

import com.typesafe.scalalogging.LazyLogging
import sttp.client4.okhttp.OkHttpSyncBackend
import sttp.client4.{Request, Response, asStringAlways, basicRequest}
import sttp.model.Uri

import scala.concurrent.duration.{Duration, DurationInt}

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
    final val Backend = OkHttpSyncBackend()
}

class HttpStatusException(msg: String) extends RuntimeException(s"HTTP failure: $msg")

trait HttpClient extends LazyLogging {
    protected def on401(): Unit = {}
    protected def addCustomHeaders[T](req: Request[T]): Request[T] = req

    protected def uriFor(path: String)(implicit config: HttpConfig): Uri =
        Uri(
            scheme = if (config.tls) "https" else "http",
            host   = config.host,
            port   = config.port,
            path   = path.split('/')
        )

    protected def doHttpRequest[T](path: String, reader: String => T)(implicit config: HttpConfig): Option[T] = {
        val req =
            addCustomHeaders(basicRequest
                .readTimeout(config.readTimeout)
                //.options(HttpOptions.allowUnsafeSSL)
                .get(uriFor(path))
                .response(asStringAlways))

        logger.debug(s"HTTP request: ${req.toString}")
        req.send(HttpClient.Backend) match {
            case Response (   _, code, _, _, _, _) if code.code == 401 => on401(); None
            case Response (body, code, _, _, _, _) if code.isSuccess   => Some(reader(body))
            case resp =>
                logger.error(s"error executing HTTP request - ${resp.toString}")
                None
        }
    }
}
