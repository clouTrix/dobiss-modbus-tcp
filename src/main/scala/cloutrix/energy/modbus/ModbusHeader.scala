package cloutrix.energy.modbus

import cloutrix.energy.internal.InPlaceEncoding
import io.netty.buffer.ByteBuf

case class ModbusHeader(transactionId: Int, protocolId: Int, pduLength: Int, unitId: Short) extends InPlaceEncoding {
    def length: Int = pduLength + 1   //+ unit identifier

    override def writeBuffer(buf: ByteBuf): ByteBuf =
        buf
            .writeShort(transactionId)
            .writeShort(protocolId)
            .writeShort(length)
            .writeByte(unitId)
}

object ModbusHeader  {
    def apply(buf: ByteBuf): ModbusHeader = ModbusHeader(
        transactionId = buf.readUnsignedShort(),
        protocolId    = buf.readUnsignedShort(),
        pduLength     = buf.readUnsignedShort(),
        unitId        = buf.readUnsignedByte()
    )
}
