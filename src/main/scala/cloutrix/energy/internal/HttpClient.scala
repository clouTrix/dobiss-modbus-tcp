package cloutrix.energy.internal

import com.typesafe.scalalogging.LazyLogging
import okhttp3.OkHttpClient
import sttp.client4.okhttp.OkHttpSyncBackend
import sttp.client4.{Request, Response, asStringAlways, basicRequest}
import sttp.model.Uri

import java.security.cert.X509Certificate
import javax.net.ssl.{SSLContext, SSLSession, X509TrustManager}
import scala.concurrent.duration.{Duration, DurationInt}
import java.time.{Duration => JDuration}

case class HttpConfig(
                         host: String,
                         port: Int,
                         connectionTimeout: Duration = HttpClient.DefaultConnectionTimeout,
                         readTimeout: Duration = HttpClient.DefaultReadTimeout,
                         tls: Boolean = false
                     )

object HttpClient {
    // TrustManager to trust any certificate
    private def trustAllCerts = new X509TrustManager {
        def checkClientTrusted(chain: Array[X509Certificate], authType: String): Unit = ()
        def checkServerTrusted(chain: Array[X509Certificate], authType: String): Unit = ()
        def getAcceptedIssuers = Array.empty[X509Certificate]
    }

    private lazy val insecureSsl =
        Some(SSLContext.getInstance("SSL"))
                .tapEach(_.init(null, Array(trustAllCerts), new java.security.SecureRandom()))
                .head

    private def clientBuilder = new OkHttpClient.Builder()
                                        .readTimeout(JDuration.ofSeconds(30))
                                        .connectTimeout(JDuration.ofSeconds(10))

    private def insecureClient = clientBuilder
                                    .hostnameVerifier((_: String, _: SSLSession) => true)
                                    .sslSocketFactory(insecureSsl.getSocketFactory, trustAllCerts)
                                    .build()

    private def secureClient = clientBuilder.build()

    final val DefaultConnectionTimeout: Duration = 10.seconds
    final val DefaultReadTimeout: Duration = 10.seconds
    final lazy val SecureBackend   = OkHttpSyncBackend.usingClient(secureClient)
    final lazy val InsecureBackend = OkHttpSyncBackend.usingClient(insecureClient)
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
        req.send(HttpClient.InsecureBackend) match {
            case Response (   _, code, _, _, _, _) if code.code == 401 => on401(); None
            case Response (body, code, _, _, _, _) if code.isSuccess   => Some(reader(body))
            case resp =>
                logger.error(s"error executing HTTP request - ${resp.toString}")
                None
        }
    }
}
