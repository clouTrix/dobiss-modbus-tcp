import cloutrix.energy.DobissModbusTcpProxy
import com.typesafe.config.{Config, ConfigValueFactory}
import envoy.Envoy
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.Eventually
import org.scalatest.matchers.should.Matchers
import org.scalatest.time.{Second, Seconds, Span}
import org.scalatest.wordspec.AnyWordSpec
import saj.SAJ
import testutils.{ByteConversions, MockHttpServer, TestSockets}

class FullAppTest extends AnyWordSpec with Matchers with BeforeAndAfterAll with TestSockets with ByteConversions with Eventually {
  private val mockEnvoy = new MockHttpServer
  private val mockSaj_1 = new MockHttpServer
  private val mockSaj_2 = new MockHttpServer

  private lazy val listeningPort = anyFreePort

  // loaded from 'application.conf' and overrides for HTTP servers
  private def configForTest(config: Config): Config = {
    config
      .withValue("modbus.tcp.port", ConfigValueFactory.fromAnyRef(Int.box(listeningPort)))
      .withValue("plugins.EnvoyS.config.port", ConfigValueFactory.fromAnyRef(Int.box(mockEnvoy.port)))
      .withValue("plugins.SAJ-1.config.port", ConfigValueFactory.fromAnyRef(Int.box(mockSaj_1.port)))
      .withValue("plugins.SAJ-2.config.port", ConfigValueFactory.fromAnyRef(Int.box(mockSaj_2.port)))
  }

  override def beforeAll(): Unit = {
    mockEnvoy.register("/ivp/meters")(Envoy.sampleMeter)
    mockEnvoy.register("/ivp/meters/readings")(Envoy.sampleMeterReadings(activePower = 10.0, actEnergyDlvd = 1000.0))
    mockSaj_1.register("/real_time_data.xml")(SAJ.sampleMeterReadings(activePower = 20.0, actEnergyDlvd = 2.0 /*kWh*/))
    mockSaj_2.register("/real_time_data.xml")(SAJ.sampleMeterReadings(activePower = 40.0, actEnergyDlvd = 4.0 /*kWh*/))

    (mockEnvoy :: mockSaj_1 :: mockSaj_2 :: Nil).foreach(_.start())
  }

  override def afterAll(): Unit = {
    (mockEnvoy :: mockSaj_1 :: mockSaj_2 :: Nil).foreach(_.stop())
  }

  "app can run and process modbus requests" in {
    val runner = new Thread(() => {
      DobissModbusTcpProxy.runReconfigured(configForTest)
      DobissModbusTcpProxy.awaitTermination()
    })

    runner.start()

    val raw30529 = Seq(0x41, 0xf7, 0x00, 0x00, 0x00, 0x06, 0x03, 0x03, 0x77, 0x41, 0x00, 0x02).map(_.toByte).toArray
    val raw30775 = Seq(0x1d, 0x7b, 0x00, 0x00, 0x00, 0x06, 0x03, 0x03, 0x78, 0x37, 0x00, 0x02).map(_.toByte).toArray

    // ERROR logs can appear here as we're issuing ModBus request while the data is not yet cached from querying the (mocked) inverters
    // (hence the 'eventually')
    eventually(timeout(Span(10, Seconds)), interval(Span(1, Second))) {
      LastInt(sendTo(listeningPort)(raw30529, 13)) should be(7000)  // all actEnergyDlvd values (in Wh) summed
      LastInt(sendTo(listeningPort)(raw30775, 13)) should be(70)    // all activePower (in W) summed
    }

    DobissModbusTcpProxy.stop()
    runner.join()
  }
}
