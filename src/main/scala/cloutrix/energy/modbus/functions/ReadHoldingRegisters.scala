package cloutrix.energy.modbus.functions

import cloutrix.energy.modbus.ModbusFunction
import io.netty.buffer.ByteBuf

case class ReadHoldingRegisters(address: Int, quantity: Int) extends ModbusFunction(0x03)

case class ReadHoldingRegistersResponse(value: Int) extends ModbusFunction(0x03) {
  def calculatedLength: Int = 1 + 1 + 4

  override def writeBuffer(buf: ByteBuf): ByteBuf =
    buf
      .writeByte(code)
      .writeByte(4)
      .writeInt(value)
}
