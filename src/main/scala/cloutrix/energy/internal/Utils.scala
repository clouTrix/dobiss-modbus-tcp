package cloutrix.energy.internal

object Utils {
    def positiveOrElse(default: Int)(vF: => Int): Int = Option(vF)
                                                            .filter(_ > 0)
                                                            .getOrElse(default)
}
