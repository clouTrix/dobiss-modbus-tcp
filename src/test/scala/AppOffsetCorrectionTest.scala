import cloutrix.energy.DobissModbusTcpProxy
import com.typesafe.config.{Config, ConfigValueFactory}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.Eventually
import org.scalatest.matchers.should.Matchers
import org.scalatest.time.{Milliseconds, Span}
import org.scalatest.wordspec.AnyWordSpec
import saj.SAJ
import testutils.{ByteConversions, MockHttpServer, TestSockets}

import scala.concurrent.duration.DurationInt

class AppOffsetCorrectionTest extends AnyWordSpec with Matchers with BeforeAndAfterAll with TestSockets with ByteConversions with Eventually {
  private val mockPV = new MockHttpServer
  private lazy val listeningPort = anyFreePort

  private val pollInterval = 1.second
  private val waitTime = Span(pollInterval.toMillis * 2, Milliseconds)
  private val waitInterval = waitTime.scaledBy(0.5)

  private def configForTest(totalProductionOffset: Int, immediateProdcutionOffset: Int) =
    (_: Config)
      .withValue("modbus.tcp.port", ConfigValueFactory.fromAnyRef(Int.box(listeningPort)))
      .withValue("poll.interval", ConfigValueFactory.fromAnyRef(s"${pollInterval.toMillis} milliseconds"))
      .withValue("corrections.total-production", ConfigValueFactory.fromAnyRef(Int.box(totalProductionOffset)))
      .withValue("corrections.immediate-production", ConfigValueFactory.fromAnyRef(Int.box(immediateProdcutionOffset)))
      .withValue("plugins.SAJ.class", ConfigValueFactory.fromAnyRef(classOf[cloutrix.energy.saj.SajDataProvider].getName))
      .withValue("plugins.SAJ.config.host", ConfigValueFactory.fromAnyRef("localhost"))
      .withValue("plugins.SAJ.config.port", ConfigValueFactory.fromAnyRef(Int.box(mockPV.port)))

  override def beforeAll(): Unit = {
    (mockPV :: Nil).foreach(_.start())
  }

  override def afterAll(): Unit = {
    (mockPV :: Nil).foreach(_.stop())
  }

  private val TotalProductionRequest = Seq(0x41, 0xf7, 0x00, 0x00, 0x00, 0x06, 0x03, 0x03, 0x77, 0x41, 0x00, 0x02).map(_.toByte).toArray
  private val ImmediateProductionRequest = Seq(0x1d, 0x7b, 0x00, 0x00, 0x00, 0x06, 0x03, 0x03, 0x78, 0x37, 0x00, 0x02).map(_.toByte).toArray

  "app modifies all meter readings with the configured offsets" when {
    "total production correction is positive" in {
      val runner = new Thread(() => {
        DobissModbusTcpProxy.runReconfigured(configForTest(totalProductionOffset = 1000, immediateProdcutionOffset = 0))
        DobissModbusTcpProxy.awaitTermination()
      })

      mockPV.register("/real_time_data.xml")(SAJ.sampleMeterReadings(activePower = 0.0, actEnergyDlvd = 0.0 /*kWh*/))
      runner.start()

      eventually(timeout(waitTime), interval(waitInterval)) {
        LastInt(sendTo(listeningPort)(TotalProductionRequest, 13)) should be(1000)
        LastInt(sendTo(listeningPort)(ImmediateProductionRequest, 13)) should be(0)
      }

      DobissModbusTcpProxy.stop()
      runner.join()
    }

    "total production correction is negative" when {
      "summed result is negative" in {
        val runner = new Thread(() => {
          DobissModbusTcpProxy.runReconfigured(configForTest(totalProductionOffset = -1000, immediateProdcutionOffset = 0))
          DobissModbusTcpProxy.awaitTermination()
        })

        mockPV.register("/real_time_data.xml")(SAJ.sampleMeterReadings(activePower = 0.0, actEnergyDlvd = 0.0 /*kWh*/))
        runner.start()

        eventually(timeout(waitTime), interval(waitInterval)) {
          LastInt(sendTo(listeningPort)(TotalProductionRequest, 13)) should be(0) // would be -1000, but limited to 0
          LastInt(sendTo(listeningPort)(ImmediateProductionRequest, 13)) should be(0)
        }

        DobissModbusTcpProxy.stop()
        runner.join()
      }

      "summed result is positive" in {
        val runner = new Thread(() => {
          DobissModbusTcpProxy.runReconfigured(configForTest(totalProductionOffset = -1000, immediateProdcutionOffset = 0))
          DobissModbusTcpProxy.awaitTermination()
        })

        mockPV.register("/real_time_data.xml")(SAJ.sampleMeterReadings(activePower = 0.0, actEnergyDlvd = 2.5 /*kWh*/))
        runner.start()

        eventually(timeout(waitTime), interval(waitInterval)) {
          LastInt(sendTo(listeningPort)(TotalProductionRequest, 13)) should be(1500)
          LastInt(sendTo(listeningPort)(ImmediateProductionRequest, 13)) should be(0)
        }

        DobissModbusTcpProxy.stop()
        runner.join()
      }
    }

    "immediate production correction is positive" in {
      val runner = new Thread(() => {
        DobissModbusTcpProxy.runReconfigured(configForTest(totalProductionOffset = 0, immediateProdcutionOffset = 1000))
        DobissModbusTcpProxy.awaitTermination()
      })

      mockPV.register("/real_time_data.xml")(SAJ.sampleMeterReadings(activePower = 0.0, actEnergyDlvd = 0.0 /*kWh*/))
      runner.start()

      eventually(timeout(waitTime), interval(waitInterval)) {
        LastInt(sendTo(listeningPort)(TotalProductionRequest, 13)) should be(0)
        LastInt(sendTo(listeningPort)(ImmediateProductionRequest, 13)) should be(1000)
      }

      DobissModbusTcpProxy.stop()
      runner.join()
    }

    "immediate production correction is negative" when {
      "summed result is negative" in {
        val runner = new Thread(() => {
          DobissModbusTcpProxy.runReconfigured(configForTest(totalProductionOffset = 0, immediateProdcutionOffset = -1000))
          DobissModbusTcpProxy.awaitTermination()
        })

        mockPV.register("/real_time_data.xml")(SAJ.sampleMeterReadings(activePower = 0.0, actEnergyDlvd = 0.0 /*kWh*/))
        runner.start()

        eventually(timeout(waitTime), interval(waitInterval)) {
          LastInt(sendTo(listeningPort)(TotalProductionRequest, 13)) should be(0)
          LastInt(sendTo(listeningPort)(ImmediateProductionRequest, 13)) should be(0) // would be -1000, but limited to 0
        }

        DobissModbusTcpProxy.stop()
        runner.join()
      }

      "summed result is positive" in {
        val runner = new Thread(() => {
          DobissModbusTcpProxy.runReconfigured(configForTest(totalProductionOffset = 0, immediateProdcutionOffset = -1000))
          DobissModbusTcpProxy.awaitTermination()
        })

        mockPV.register("/real_time_data.xml")(SAJ.sampleMeterReadings(activePower = 2500.0, actEnergyDlvd = 0.0 /*kWh*/))
        runner.start()

        eventually(timeout(waitTime), interval(waitInterval)) {
          LastInt(sendTo(listeningPort)(TotalProductionRequest, 13)) should be(0)
          LastInt(sendTo(listeningPort)(ImmediateProductionRequest, 13)) should be(1500)
        }

        DobissModbusTcpProxy.stop()
        runner.join()
      }
    }
  }
}
