package cloutrix.energy.saj

import cloutrix.energy.internal.{DataProviderCache, HttpConfig, HttpDataPoller}
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging

class SajDataProvider(config: Config) extends HttpDataPoller with DataProviderCache with LazyLogging {
  implicit val httpConfig: HttpConfig = HttpConfig(host = config.getString("host"), port = config.getInt("port"))

  register("readings" -> ("/real_time_data.xml", SajMeterReading.fromXml), autoStart = true)

  override def onError(cause: Throwable): Unit = {
    cause match {
      case _: java.net.ConnectException =>
        logger.warn(s"Unable to connect to ${httpConfig.host} on port ${httpConfig.port}, assume off-line")
        cache(currentProduction = Some(0))

      case ex: org.xml.sax.SAXParseException =>
        logger.error(s"Error decoding XML from inverter (${httpConfig.host}:${httpConfig.port}): ${ex.getMessage}")

      case _ =>
        super.onError(cause)
    }
  }

  override def onData(id: String, data: Any): Unit = {
    data match {
      case dd: SajMeterReading =>
        logger.info(s"current production data: ${dd}")
        // p-ac    is expressed in 'W', so OK for what Dobiss-NXT expects
        // e-total is expressed in 'kWh', so we need to convert it to 'Wh' what Dobiss-NXT expects
        cache(currentProduction = Some(dd.pAc.toInt), totalProduction = Some((dd.eTotal * 1000.0).toInt))

      case _ =>
        logger.error(s"unhandled data delivery - id: ${id}, data: ${data}")
    }
  }
}
