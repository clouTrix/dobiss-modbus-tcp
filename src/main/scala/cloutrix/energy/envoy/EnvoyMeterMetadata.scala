package cloutrix.energy.envoy

import com.github.plokhotnyuk.jsoniter_scala.core.readFromString
import com.github.plokhotnyuk.jsoniter_scala.macros.JsonCodecMaker

case class EnvoyMeterMeta(eid: Long, measurementType: String)
case class EnvoyMeterMetas(all: Seq[EnvoyMeterMeta])

object EnvoyMeterMetadata {
  private val JsonCodec = JsonCodecMaker.make[Seq[EnvoyMeterMeta]]
  val read : String => EnvoyMeterMetas = (raw: String) => EnvoyMeterMetas(all = readFromString(raw)(EnvoyMeterMetadata.JsonCodec))
}
