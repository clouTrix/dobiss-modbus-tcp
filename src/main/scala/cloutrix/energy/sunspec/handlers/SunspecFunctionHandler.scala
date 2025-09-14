package cloutrix.energy.sunspec.handlers

import cloutrix.energy.modbus.ModbusFrame
import cloutrix.energy.modbus.functions.{ReadHoldingRegisters, ReadHoldingRegistersResponse}
import cloutrix.energy.sunspec.SunspecDataMapper
import com.typesafe.scalalogging.LazyLogging
import io.netty.channel.{ChannelHandlerContext, ChannelInboundHandlerAdapter}
import io.netty.handler.timeout.IdleStateEvent
import io.netty.util.ReferenceCountUtil

class SunspecFunctionHandler(sunspecMappings: SunspecDataMapper) extends ChannelInboundHandlerAdapter with LazyLogging {
    override def userEventTriggered(ctx: ChannelHandlerContext, evt: Any): Unit = {
        logger.debug(s"user event received - channel:${ctx.channel()}, event:${evt}")

        evt match {
            case _: IdleStateEvent =>
                logger.warn(s"nothing happening on the channel, closing it - channel:${ctx.channel()}")
                ctx.close()

            case _ =>
                super.userEventTriggered(ctx, evt)
        }
    }

    override def channelActive(ctx: ChannelHandlerContext): Unit = {
        logger.debug(s"channel active - channel:${ctx.channel()}")
    }

    override def channelInactive(ctx: ChannelHandlerContext): Unit = {
        logger.debug(s"channel inactive - channel:${ctx.channel()}")
    }

    override def channelRead(ctx: ChannelHandlerContext, msg: Any): Unit = {
        logger.debug(s"channel read - channel:${ctx.channel()}, message:${msg}")

        msg match {
            case ModbusFrame(hdr, ReadHoldingRegisters(address, _)) if sunspecMappings.isDefinedAt(address) =>
                val response = ReadHoldingRegistersResponse( sunspecMappings(address) )
                ctx.write(
                    ModbusFrame(header = hdr.copy(pduLength = response.calculatedLength), function = response)
                )

            case _ =>
                logger.error(s"unsupported modbus request, closing channel - message: ${msg}")
                ctx.close()
        }

        ReferenceCountUtil.release(msg)
    }

    override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable): Unit = {
        logger.error(s"exception fired on channel, closing it. - channel: ${ctx.channel()}, cause: ${cause.toString}}")
        ctx.close()
    }

    override def channelReadComplete(ctx: ChannelHandlerContext): Unit = {
        logger.debug(s"channel read complete - channel:${ctx.channel()}")
        ctx.flush()
    }
}
