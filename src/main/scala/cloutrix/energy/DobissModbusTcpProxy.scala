package cloutrix.energy

import cloutrix.energy.internal.Utils.positiveOrElse
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
        // we need to make sure the returned number is always >= 0
        // as Dobiss can not deal with negative numbers because of a mis-interpretation on their side.
        // SunSpec describes the registers as SIGNED, while Dobiss-NXT basically throws away the sign
        // which would result in Dobiss-NXT showing 65526W production instead of -10W (10W consumption)
        // ( and EnvoyS remains online during the night, showing negative values for production )
        Sunspec.AccumulatedCurrentActivePower.address -> (() => positiveOrElse(0)(dataProvider.currentProduction + config.immediateProductionCorrection)),
        Sunspec.TotalYieldWh.address                  -> (() => positiveOrElse(0)(dataProvider.totalProduction + config.totalProductionCorrection))
      ))
    )

    server.start()
    TaskScheduling.findAll(providers: _*).foreach(_.start(config.pollInterval))

    stop = () => {
      logger.info("shutdown triggered, close the application")
      server.stop()
      TaskScheduling.findAll(providers: _*).foreach(_.stop())
      server.awaitTermination()
      logger.info(s"Dobiss ModBus-TCP proxy - Bye bye!")
    }

    awaitTermination = () => server.awaitTermination()
    System.gc()
  }

  // main entry point
  def main(args: Array[String]): Unit = {
    runReconfigured(identity)
    sys.addShutdownHook { stop() }
    awaitTermination()
  }
}
