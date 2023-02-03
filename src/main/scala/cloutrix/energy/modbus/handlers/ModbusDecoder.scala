package cloutrix.energy.modbus.handlers

import cloutrix.energy.modbus.{Modbus, ModbusFrame}
import com.typesafe.scalalogging.LazyLogging
import io.netty.buffer.{ByteBuf, ByteBufUtil}
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder

import java.util.{List => JList}

class ModbusDecoder extends ByteToMessageDecoder with LazyLogging {
  override def decode(ctx: ChannelHandlerContext, buf: ByteBuf, out: JList[AnyRef]): Unit = {
    logger.debug(s"decode [${ctx.channel()}] - buffer: ${buf}, data: ${ByteBufUtil.hexDump(buf)}")

    if(buf.readableBytes() < (Modbus.MBAP_LENGTH + 1)) return // + function code

    out.add(
      ModbusFrame.decode(buf)
    )
  }
}
