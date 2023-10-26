package cloutrix.energy.envoy

import cloutrix.energy.internal.{DataProviderCache, HttpClient, HttpConfig, HttpDataPoller}
import com.github.plokhotnyuk.jsoniter_scala.core.readFromString
import com.github.plokhotnyuk.jsoniter_scala.macros.JsonCodecMaker
import com.typesafe.config.Config
import com.typesafe.scalalogging.{LazyLogging, StrictLogging}
import scalaj.http.{Http, HttpOptions, HttpRequest, HttpResponse, HttpStatusException}

import java.net.{URL, URLEncoder}
import java.nio.charset.StandardCharsets
import scala.util.{Failure, Success, Try}

case class Session(session_id: String, manager_token : String)
object Session {
  private val JsonCodec = JsonCodecMaker.make[Session]
  val read: String => Session = readFromString(_: String)(JsonCodec)
}

case class Token(generation_time: Long, token: String, expires_at: Long)
object Token {
  private val JsonCodec = JsonCodecMaker.make[Token]
  val read: String => Token = readFromString(_: String)(JsonCodec)
}

object EnvoyDataProvider extends StrictLogging {
  def defaultTokenProvider(config: Config): Option[String] = {
    Try(config.getString("username") -> config.getString("password")) match {
      case Success(username -> password) =>
        val tokenMaybe = onManagementSession(username, password)(config)
          .flatMap(fetchJwt(_, config.getString("serial"), username)(config))

        tokenMaybe match {
          case Success(token) =>
            logger.info("JWT token successfully refreshed")
            Some(token)

          case Failure(cause) =>
            logger.error(s"error refreshing JWT token - cause: ${cause.toString}")
            None
        }

      // URLs not configured -> no need to fetch tokens
      case _ => None
    }
  }

  private def fetchJwt(sessionId: String, serial: String, username: String)(config: Config): Try[String] = {
    def req = Http(config.getString("token.url"))
                .timeout(30000, 30000)
                .header("Content-Type", "application/json")
                .postData( s"""| {
                               |   "session_id": "${sessionId}",
                               |   "serial_num": "${serial}",
                               |   "username": "${username}"
                               | }
                               """.stripMargin )

    logger.warn(s"request new JWT token - sessionId: $sessionId, serial: $serial, username: $username")
    req.asString match {
      //FIXME: althoug Content-Type is set to 'application/json', the API returns plain text
      //case resp if resp.is2xx => Try(Token.read(resp.body).token)
      case resp if resp.is2xx => Success(resp.body)

      case resp =>
        Failure(HttpStatusException(resp.code, resp.header("Status").getOrElse("UNKNOWN"), resp.body.toString))
    }
  }

  private def onManagementSession(username: String, password: String)(config: Config): Try[String] = {
    def req = Http(config.getString("login.url"))
      .timeout(30000, 30000)
      .postForm(params = Seq(
          "user[email]" -> username,
          "user[password]" -> password
      ))

    logger.warn(s"request new Management session - username: $username")
    req.asString match {
      case resp if resp.is2xx => Try(Session.read(resp.body).session_id)
      case resp =>
        Failure(HttpStatusException(resp.code, resp.header("Status").getOrElse("UNKNOWN"), resp.body.toString))
    }
  }
}

class EnvoyDataProvider(config: Config, tokenProvider: Config => Option[String]) extends HttpDataPoller with DataProviderCache with LazyLogging {
  def this(config: Config) = this(config, EnvoyDataProvider.defaultTokenProvider)
  implicit val httpConfig: HttpConfig = HttpConfig(
    host = config.getString("host"),
    port = config.getInt("port"),
    tls  = Try(config.getBoolean("tls")).getOrElse(false)
  )

  register("readings" -> ( "/ivp/meters/readings" , EnvoyMeterReading.read ), autoStart = false )
  register("metadata" -> ( "/ivp/meters"          , EnvoyMeterMetadata.read), autoStart = true  )

  private var apiToken: Option[String] = tokenProvider(config)

  // will only be called upon reception of a 401
  override def on401(): Unit = {
    logger.warn("401 received - refresh api-token")
    apiToken = tokenProvider(config)
  }

  override def addCustomHeaders(req: HttpRequest): HttpRequest =
                  apiToken
                    .map(token => req.header("Authorization", "Bearer %s".format(token)))
                    .getOrElse( req )

  private val metadataHandler: PartialFunction[(String, Any), Long] = {
    case (_, d: EnvoyMeterMetas) if d.all.exists(_.measurementType == "production") =>
            d.all.find(_.measurementType == "production")
              .tapEach(_ => cancelTask("metadata"))
              .map(_.eid)
              .tapEach(eid => logger.info(s"production eId: ${eid}"))
              .head
  }

  private val logAndIgnore: PartialFunction[(String, Any), Unit] = {
    case (id: String, data: Any) => logger.warn(s"unhandled data delivery - id: ${id}, data: ${data}")
  }

  private def readingsHandler(eid: Long): PartialFunction[(String, Any), Unit] = {
    startTask("readings")

    {
      case (_, d: EnvoyMeterReadings) =>
        d.all.find(_.eid == eid)
          .tapEach(dd => logger.info(s"current production data: ${dd}"))
          .foreach(dd =>
            cache(currentProduction = Some(dd.activePower.toInt), totalProduction = Some(dd.actEnergyDlvd.toInt))
          )
    }
  }

  private var dataHandler: PartialFunction[(String, Any), Unit] = metadataHandler andThen { eid => dataHandler = readingsHandler(eid) }

  override def onData(id: String, dataMaybe: Option[Any]): Unit = dataMaybe.foreach((dataHandler orElse logAndIgnore)(id, _))

  override def onError(cause: Throwable): Unit =
    logger.warn(s"error processing HTTP request, ignore and retry - cause: ${cause.toString}")
}
