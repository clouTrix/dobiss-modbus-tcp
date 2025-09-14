package cloutrix.energy.envoy

import cloutrix.energy.internal._
import com.github.plokhotnyuk.jsoniter_scala.core.{JsonValueCodec, readFromString}
import com.github.plokhotnyuk.jsoniter_scala.macros.JsonCodecMaker
import com.typesafe.config.Config
import com.typesafe.scalalogging.{LazyLogging, StrictLogging}
import sttp.client4._
import sttp.client4.jsoniter._

import scala.concurrent.duration.DurationInt
import scala.util.{Failure, Success, Try}

case class Session(session_id: String, manager_token: String)
object Session {
    val JsonCodec: JsonValueCodec[Session] = JsonCodecMaker.make[Session]
    val read: String => Session = readFromString(_: String)(JsonCodec)
}

//FIXME: although Content-Type is set to 'application/json', the API returns plain text
//
//case class Token(generation_time: Long, token: String, expires_at: Long)
//object Token {
//  private val JsonCodec = JsonCodecMaker.make[Token]
//  val read: String => Token = readFromString(_: String)(JsonCodec)
//}

object EnvoyDataProvider extends StrictLogging {
    def defaultTokenProvider(config: Config): Option[String] = {
        //HACK to allow preconfigured token
        if(Try(config.getString("jwt-token")).isSuccess) {
            logger.warn("pre-configured JWT token found, do not acquire one")
            return Some(config.getString("jwt-token"))
        }

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

    def fetchJwt(sessionId: String, serial: String, username: String)(config: Config): Try[String] = {
        def req =
            basicRequest
                .readTimeout(30.seconds)
                .header("Content-Type", "application/json")
                .body(StringBody(
                    s"""| {
                        |   "session_id": "${sessionId}",
                        |   "serial_num": "${serial}",
                        |   "username": "${username}"
                        | }
                     """.stripMargin,
                    "utf-8"
                ))
                .post(uri"${config.getString("token.url")}")
                .response(asStringAlways)

        logger.info(s"request new JWT token - sessionId: $sessionId, serial: $serial, username: $username")

        logger.debug(s"HTTP request: ${req.toString}")
        req.send(HttpClient.Backend) match {
            case resp @ Response(body, statusCode, _, _, _, _) if statusCode.isSuccess =>
                logger.debug(s"HTTP response: ${resp.toString}")
                Success(body)

            case resp =>
                Failure(new HttpStatusException(resp.toString))
        }
    }

    def onManagementSession(username: String, password: String)(config: Config): Try[String] = {
        implicit val codec: JsonValueCodec[Session] = Session.JsonCodec

        def req =
            basicRequest.multipartBody(
                    multipart("user[email]"   , username),
                    multipart("user[password]", password))
                .post(uri"${config.getString("login.url")}")
                .response(asJsonOrFail[Session])

        logger.info(s"request new Management session - username: $username")

        logger.debug(s"HTTP request: ${req.toString}")
        req.send(HttpClient.Backend) match {
            case resp @ Response(body, statusCode, _, _, _, _) if statusCode.isSuccess =>
                logger.debug(s"HTTP response: ${resp.toString}")
                Success(body.session_id)
            case resp =>
                Failure(new HttpStatusException(resp.toString))
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

    register("readings" -> ( "ivp/meters/readings" , EnvoyMeterReading.read ), autoStart = false )
    register("metadata" -> ( "ivp/meters"          , EnvoyMeterMetadata.read), autoStart = true  )

    private var apiToken: Option[String] = tokenProvider(config)

    // will only be called upon reception of a 401
    override def on401(): Unit = {
        logger.warn("401 received - refresh api-token")
        apiToken = tokenProvider(config)
    }

    override def addCustomHeaders[T](req: Request[T]): Request[T] =
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
        logger.error(s"error processing HTTP request, ignore and retry - cause: ${cause.toString}")
}
