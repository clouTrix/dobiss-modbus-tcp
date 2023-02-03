package cloutrix.energy.modbus

import cloutrix.energy.internal.InPlaceEncoding
import cloutrix.energy.modbus.functions.ReadHoldingRegisters
import com.typesafe.scalalogging.LazyLogging
import io.netty.buffer.ByteBuf

abstract class ModbusFunction(val code: Byte) extends InPlaceEncoding

object ModbusFunction extends LazyLogging {
  def apply[T >: ModbusFunction](buf: ByteBuf): T = {
    buf.readUnsignedByte() match {
      case 0x03 => ReadHoldingRegisters(
        address  = buf.readShort(),
        quantity = buf.readShort()
      )

      case code =>
        throw new IllegalArgumentException(s"unsupported function code: ${code}")
    }
  }
}
