package cloutrix.energy.sunspec

class SunspecDataMapper(mappings: Map[Int, () => Int]) {
    def isDefinedAt(code: Int): Boolean = mappings.contains(code)
    def apply(code: Int): Int = mappings.getOrElse(code, throw new IllegalAccessException(s"sunspec register ${code} not mapped"))()
}
