package envoy

import cloutrix.energy.envoy.EnvoyDataProvider
import cloutrix.energy.internal.AccumulatingDataProvider
import com.typesafe.config.{ConfigFactory, ConfigValueFactory}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.Eventually
import org.scalatest.matchers.should.Matchers
import org.scalatest.time.{Seconds, Span}
import org.scalatest.wordspec.AnyWordSpec
import testutils.MockHttpServer

import java.util.concurrent.{Executors, ScheduledExecutorService}
import scala.concurrent.duration.DurationInt

class DataProviderTests extends AnyWordSpec with Matchers with BeforeAndAfterAll with Eventually {
    private val mockHttp = new MockHttpServer

    private val config = ConfigFactory.empty()
        .withValue("host", ConfigValueFactory.fromAnyRef("localhost"))
        .withValue("port", ConfigValueFactory.fromAnyRef(Int.box(mockHttp.port)))

    private implicit val scheduler: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()

    override def beforeAll(): Unit = {
        mockHttp.start()
    }

    override def afterAll(): Unit = mockHttp.stop()

    "data can be scraped from EnvoyS on interval" when {
        "only a single one is configured" in {
            mockHttp.register("/ivp/meters")(Envoy.sampleMeter)
            mockHttp.register("/ivp/meters/readings")(Envoy.sampleMeterReadings(activePower = 666.0, actEnergyDlvd = 777.0))

            val provider = new EnvoyDataProvider(config)
            provider.start(1.second)

            eventually(timeout(Span(10, Seconds))) {
                provider.currentProduction should be(666)
                provider.totalProduction should be(777)
            }

            provider.stop()
        }
    }

    "multiple ones are aggregated through an AccumulatingDataProvider" in {
        mockHttp.register("/ivp/meters")(Envoy.sampleMeter)
        mockHttp.register("/ivp/meters/readings")(Envoy.sampleMeterReadings(activePower = 111.0, actEnergyDlvd = 222.0))

        val COUNT = 3
        val providers = (0 until COUNT).map(_ => new EnvoyDataProvider(config))
        val provider = new AccumulatingDataProvider(providers)
        providers.foreach(_.start(1.second))

        eventually(timeout(Span(10, Seconds))) {
            provider.currentProduction should be(111 * COUNT)
            provider.totalProduction should be(222 * COUNT)
        }

        providers.foreach(_.stop())
    }
}
