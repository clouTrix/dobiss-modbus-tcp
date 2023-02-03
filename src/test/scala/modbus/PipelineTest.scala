package modbus

import cloutrix.energy.modbus.handlers.ModbusChannelInitializer
import cloutrix.energy.sunspec.SunspecDataMapper
import io.netty.buffer.{ByteBuf, ByteBufUtil, Unpooled}
import io.netty.channel.embedded.EmbeddedChannel
import io.netty.util.ResourceLeakDetector
import io.netty.util.ResourceLeakDetector.Level
import org.scalatest.Inside
import org.scalatest.concurrent.Eventually
import org.scalatest.matchers.should.Matchers
import org.scalatest.time.{Milliseconds, Seconds, Span}
import org.scalatest.wordspec.AnyWordSpec
import testutils.Profiling

import scala.concurrent.duration.DurationInt

class PipelineTest extends AnyWordSpec with Matchers with Inside with Eventually with Profiling {
  ResourceLeakDetector.setLevel(Level.PARANOID)

  private val mapper = new SunspecDataMapper(Map(
    30529 -> (() => 666),
    30775 -> (() => 777)
  ))

  private def withChannel[T](f: EmbeddedChannel => T) = {
    val ch = new EmbeddedChannel()
    new ModbusChannelInitializer(mapper).initChannel(ch) // full pipeline
    ch.pipeline().fireChannelActive() // trigger activation

    f(ch)
    ch.finishAndReleaseAll()
  }

  "full pipeline" should {
    "close the channel" when {
      "an unsupported Modbus function is requested" in {
        val raw30666 = Seq(0x41, 0xf7, 0x00, 0x00, 0x00, 0x06, 0x03, 0x03, 0x77, 0xCA, 0x00, 0x02).map(_.toByte).toArray

        withChannel { ch =>
          ch.writeInbound(Unpooled.wrappedBuffer(raw30666))
          eventually(timeout(Span(5, Seconds)))(ch.isActive should be (false))   // channel closed by service
        }
      }

      "nothing got received on the channel for 10 seconds" in {
        withChannel { ch =>
          val d = Time {
            eventually(timeout(Span(15, Seconds)), interval(Span(100, Milliseconds))) {
              ch.runPendingTasks()
              ch.isActive should be(false)
            }
          }

          d.toMillis should (be > 10.seconds.toMillis)
        }
      }
    }

    "be able to handle chunked input" in {
      val raw30529_1 = Seq(0x41, 0xf7, 0x00, 0x00, 0x00, 0x06, 0x03).map(_.toByte).toArray
      val raw30529_2 = Seq(0x03, 0x77, 0x41, 0x00, 0x02).map(_.toByte).toArray

      withChannel { ch =>
        ch.writeInbound(Unpooled.wrappedBuffer(raw30529_1))
        ch.writeInbound(Unpooled.wrappedBuffer(raw30529_2))
        inside(ch.outboundMessages().poll()) {
          case buf: ByteBuf =>
            ch.isActive should be(true)
            ByteBufUtil.getBytes(buf).toSeq should contain theSameElementsInOrderAs Seq(
              0x41, 0xf7, 0x00, 0x00, 0x00, 0x07, 0x03, 0x03, 0x04, 0x00, 0x00, 0x02, 0x9a).map(_.toByte)

            buf.release()
        }
      }
    }

    "not introduce memory leaks" in {
      val raw30529 = Seq(0x41, 0xf7, 0x00, 0x00, 0x00, 0x06, 0x03, 0x03, 0x77, 0x41, 0x00, 0x02).map(_.toByte).toArray
      val raw30775 = Seq(0x1d, 0x7b, 0x00, 0x00, 0x00, 0x06, 0x03, 0x03, 0x78, 0x37, 0x00, 0x02).map(_.toByte).toArray

      def test(ar: Array[Byte], expected: Seq[Byte]) = withChannel { ch =>
        ch.writeInbound(Unpooled.wrappedBuffer(ar))
        inside(ch.outboundMessages().poll()) {
          case buf: ByteBuf =>
            ch.isActive should be (true)
            ByteBufUtil.getBytes(buf).toSeq should contain theSameElementsInOrderAs expected
            buf.release()
        }
      }

      Loop(10.seconds, 10.milliseconds) {
        test(raw30529, Seq(0x41, 0xf7, 0x00, 0x00, 0x00, 0x07, 0x03, 0x03, 0x04, 0x00, 0x00, 0x02, 0x9a).map(_.toByte))
        test(raw30775, Seq(0x1d, 0x7b, 0x00, 0x00, 0x00, 0x07, 0x03, 0x03, 0x04, 0x00, 0x00, 0x03, 0x09).map(_.toByte))
      }

      System.gc()
    }
  }
}
