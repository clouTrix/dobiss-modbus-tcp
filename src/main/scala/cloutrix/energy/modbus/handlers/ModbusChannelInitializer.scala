package cloutrix.energy.modbus.handlers

import cloutrix.energy.sunspec.SunspecDataMapper
import cloutrix.energy.sunspec.handlers.SunspecFunctionHandler
import com.typesafe.scalalogging.LazyLogging
import io.netty.channel.{Channel, ChannelInitializer}
import io.netty.handler.timeout.IdleStateHandler

class ModbusChannelInitializer(sunspecMappings: SunspecDataMapper) extends ChannelInitializer[Channel] with LazyLogging {
    override def initChannel(ch: Channel): Unit = {
        logger.debug(s"new connection - channel: ${ch}")

        ch.pipeline()
            .addLast("modbus-framer" , new ModbusFrameDecoder)
            .addLast("modbus-encoder", new ModbusEncoder)
            .addLast("modbus-decoder", new ModbusDecoder)
            .addLast("", new IdleStateHandler(10,10,0))
            .addLast("sunspec-handler", new SunspecFunctionHandler(sunspecMappings))
    }
}
