package cloutrix.energy

import cloutrix.energy.internal._
import cloutrix.energy.sunspec.{Sunspec, SunspecDataMapper}
import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.StrictLogging

import java.util.concurrent.{Executors, ScheduledExecutorService}

object DobissModbusTcpProxy extends StrictLogging {
  logger.info("start Dobiss ModBus-TCP proxy")

  var stop: () => Unit = () => {}
  var awaitTermination: () => Unit = () => {}

  def runReconfigured(reconf: Config => Config): Unit = {
    val config = AppConfig.load(reconf(ConfigFactory.load()))
    implicit val taskScheduler: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()

    val providers = Plugin.load(config.plugins: _*)
    val dataProvider = new AccumulatingDataProvider(providers)
    val server = new ModbusServer(
      config,
      new SunspecDataMapper(Map(
        Sunspec.AccumulatedCurrentActivePower.address -> (() => dataProvider.currentProduction),
        Sunspec.TotalYieldWh.address -> (() => dataProvider.totalProduction)
      ))
    )

    server.start()
    TaskScheduling.findAll(providers: _*).foreach(_.start(config.pollInterval))

    stop = () => {
      logger.info("shutdown triggered, close the application")
      server.stop()
      TaskScheduling.findAll(providers: _*).foreach(_.stop())
      server.awaitTermination()
    }

    awaitTermination = () => server.awaitTermination()

    System.gc()
    logger.info(s"Dobiss ModBus-TCP proxy - Bye bye!")
  }

  // main entry point
  def main(args: Array[String]): Unit = {
    runReconfigured(identity)
    sys.addShutdownHook { stop() }
    awaitTermination()
  }
}
