package testutils

import java.nio.ByteBuffer

trait ByteConversions {
    def LastInt(a: => Array[Byte]): Int = ByteBuffer.wrap(a.takeRight(4)).getInt
}
