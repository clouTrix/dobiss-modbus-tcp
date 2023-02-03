package cloutrix.energy.modbus

import cloutrix.energy.internal.InPlaceEncoding
import io.netty.buffer.ByteBuf

case class ModbusFrame(header: ModbusHeader, function: ModbusFunction with InPlaceEncoding) extends InPlaceEncoding {
  override def writeBuffer(buf: ByteBuf): ByteBuf =
    Option(buf)
      .map(header.writeBuffer)
      .map(function.writeBuffer)
      .getOrElse(
        throw new IllegalStateException("unable to encode Modbus frame")
      )
}

object ModbusFrame  {
  def decode(buf: ByteBuf): ModbusFrame = ModbusFrame(
    header   = ModbusHeader(buf),
    function = ModbusFunction(buf)
  )
}
