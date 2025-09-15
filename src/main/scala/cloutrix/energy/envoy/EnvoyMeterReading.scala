package cloutrix.energy.envoy

import com.github.plokhotnyuk.jsoniter_scala.core.readFromString
import com.github.plokhotnyuk.jsoniter_scala.macros.JsonCodecMaker

case class EnvoyMeterReading(eid: Long, activePower: Double, actEnergyDlvd: Double)
case class EnvoyMeterReadings(all: Seq[EnvoyMeterReading])

object EnvoyMeterReading {
    private val JsonCodec = JsonCodecMaker.make[Seq[EnvoyMeterReading]]
    val read : String => EnvoyMeterReadings = (raw: String) => EnvoyMeterReadings(all = readFromString(raw)(EnvoyMeterReading.JsonCodec))
}
