package cloutrix.energy.internal

import cloutrix.energy.modbus.handlers.ModbusChannelInitializer
import cloutrix.energy.sunspec.SunspecDataMapper
import com.typesafe.scalalogging.LazyLogging
import io.netty.bootstrap.ServerBootstrap
import io.netty.buffer.PooledByteBufAllocator
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.channel.{Channel, ChannelFuture, ChannelOption, EventLoopGroup}

import java.util.concurrent.TimeUnit

/**
 *
 */
class ModbusServer(config: AppConfig, sunspecMappings: SunspecDataMapper) extends LazyLogging {
  private var serverChannel: Channel = _

  def start(): Unit = {
    serverChannel = ModbusServer.createServiceListener(sunspecMappings)
      .bind(config.modbusTcpPort)
      .sync()
      .channel()

    serverChannel.closeFuture().addListener((f: ChannelFuture) => {
      logger.info(s"service channel ${f.channel().localAddress()} closed - shutting down service")
      ModbusServer.shutdownWorkers()
    })

    logger.info(s"Modbus-TCP server running on channel: ${serverChannel}")
  }

  def stop(): Unit = {
    logger.info("stop server on user request")
    serverChannel.close().awaitUninterruptibly()
  }

  def awaitTermination(): Unit = try { ModbusServer.workerGroup.awaitTermination(Long.MaxValue, TimeUnit.NANOSECONDS) } catch { case _: InterruptedException => }
}

/**
 *
 */
object ModbusServer {
  lazy val bossGroup: EventLoopGroup = new NioEventLoopGroup(1)
  lazy val workerGroup: EventLoopGroup = bossGroup

  def shutdownWorkers(): Unit = {
    ModbusServer.bossGroup.shutdownGracefully()
    ModbusServer.workerGroup.shutdownGracefully()
  }

  def createServiceListener(sunspecMappings: SunspecDataMapper): ServerBootstrap = {
    new ServerBootstrap()
      .group(bossGroup, workerGroup)
      .channel(classOf[NioServerSocketChannel])
      .option(ChannelOption.SO_BACKLOG, Int.box(4))
      .childOption(ChannelOption.SO_KEEPALIVE, Boolean.box(true))
      .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
      .childHandler(new ModbusChannelInitializer(sunspecMappings))
  }
}
