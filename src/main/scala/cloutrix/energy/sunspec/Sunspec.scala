package cloutrix.energy.sunspec

// ------+--------------------------------------------------+----+-----+------+----
// 30775 | Current active power on all line conductors (W)  | 2  | S32 | FIX0 | RO
//       | accumulated values of the inverters              |    |     |      |
// ------+--------------------------------------------------+----+-----+------+----
// 30529 | Total yield (Wh) [E-Total]                       | 2  | U32 | FIX0 | RO
// ------+--------------------------------------------------+----+-----+------+----

object Sunspec {
  sealed trait Register { def address: Int }
  final case object AccumulatedCurrentActivePower extends Register { val address = 30775 }
  final case object TotalYieldWh                  extends Register { val address = 30529 }
}
