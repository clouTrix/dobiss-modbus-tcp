package cloutrix.energy.saj

import cloutrix.energy.internal.{DataProviderCache, HttpConfig, HttpDataPoller}
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging

class SajDataProvider(config: Config) extends HttpDataPoller with DataProviderCache with LazyLogging {
  implicit val httpConfig: HttpConfig = HttpConfig(host = config.getString("host"), port = config.getInt("port"))

  register("readings" -> ( "/real_time_data.xml" , SajMeterReading.fromXml ), autoStart = true )

  override def onData(id: String, data: Any): Unit = {
    data match {
      case saj: SajMeterReading =>
        //TODO: verify!!
        // p-ac    is expressed in 'W', so OK for what Dobiss-NXT expects
        // e-total is expressed in 'kWh', so we need to convert it to 'Wh' what Dobiss-NXT expects
        cache(currentProduction = Some(saj.pAc.toInt), totalProduction = Some((saj.eTotal * 1000.0).toInt))

      case _ =>
        logger.warn(s"unhandled data delivery - id: ${id}, data: ${data}")
    }
  }
}
