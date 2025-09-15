package cloutrix.energy.saj

import scala.xml.{Elem, XML}


case class SajMeterReading(pAc: Double, eTotal: Double)

object SajMeterReading {
    private final val ActivePower = "p-ac"
    private final val TotalYield  = "e-total"

    private def asDouble(xml: Elem)(nodeName: String): Double = (xml \\ nodeName).head.text.toDouble

    val fromXml: String => SajMeterReading = (raw) => {
        //keep in mind: SAJ has it all ... CamelCasing, snake_casing, node-names with dashes in it, ...
        // might not be a bad idea to convert to lowercase alphanumerics only to prevent future problems
        // as this raises doubts in how well they comply to their own "standards" ...
        val extract = asDouble(XML.loadString(raw))(_)

        SajMeterReading(
            pAc    = extract(ActivePower),
            eTotal = extract(TotalYield)
        )
    }
}
