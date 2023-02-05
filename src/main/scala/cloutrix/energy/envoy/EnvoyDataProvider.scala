package cloutrix.energy.envoy

import cloutrix.energy.internal.{DataProviderCache, HttpConfig, HttpDataPoller}
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging

class EnvoyDataProvider(config: Config) extends HttpDataPoller with DataProviderCache with LazyLogging {
  implicit val httpConfig: HttpConfig = HttpConfig(host = config.getString("host"), port = config.getInt("port"))

  register("readings" -> ( "/ivp/meters/readings" , EnvoyMeterReading.read ), autoStart = false )
  register("metadata" -> ( "/ivp/meters"          , EnvoyMeterMetadata.read), autoStart = true  )

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

  override def onData(id: String, data: Any): Unit = (dataHandler orElse logAndIgnore)(id, data)
}
