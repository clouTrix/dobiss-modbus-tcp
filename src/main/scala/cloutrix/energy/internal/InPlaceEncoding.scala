package cloutrix.energy.internal

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext

trait InPlaceEncoding {
  final def encode(ctx: ChannelHandlerContext)(buf: ByteBuf = ctx.alloc().buffer()): ByteBuf = writeBuffer(buf)
  def writeBuffer(buf: ByteBuf): ByteBuf = buf
}
