package cloutrix.energy.modbus.handlers

import cloutrix.energy.modbus.ModbusFrame
import com.typesafe.scalalogging.LazyLogging
import io.netty.channel.{ChannelHandlerContext, ChannelOutboundHandlerAdapter, ChannelPromise}

/**
 *
 */
class ModbusEncoder extends ChannelOutboundHandlerAdapter with LazyLogging {
  override def write(ctx: ChannelHandlerContext, msg: Any, promise: ChannelPromise): Unit = {
    logger.debug(s"write [${ctx.channel()}] - msg: ${msg}")

    msg match {
      case frame: ModbusFrame => ctx.write(frame.encode(ctx)(), promise)
      case _                  => super.write(ctx, msg, promise)
    }
  }
}
