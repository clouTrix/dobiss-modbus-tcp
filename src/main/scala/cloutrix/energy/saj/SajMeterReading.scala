package cloutrix.energy.saj

import scala.xml.{Elem, XML}


case class SajMeterReading(pAc: Double, eTotal: Double)

object SajMeterReading {
  private final val ActivePower = "p-ac"
  private final val TotalYield  = "e-total"

  private def asDouble(xml: Elem)(nodeName: String): Double = (xml \\ nodeName).head.text.toDouble

  val fromXml: String => SajMeterReading = (raw) => {
    val extract = asDouble(XML.loadString(raw))(_)

    SajMeterReading(
      pAc    = extract(ActivePower),
      eTotal = extract(TotalYield)
    )
  }
}
