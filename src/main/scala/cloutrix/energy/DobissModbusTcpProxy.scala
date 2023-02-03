package cloutrix.energy

import cloutrix.energy.internal._
import cloutrix.energy.sunspec.{Sunspec, SunspecDataMapper}
import com.typesafe.scalalogging.LazyLogging

import java.util.concurrent.{Executors, ScheduledExecutorService}

object DobissModbusTcpProxy extends App with LazyLogging {
  logger.info("start Dobiss ModBus-TCP proxy")

  private val config = AppConfig.load()
  implicit private val taskScheduler: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()

  private val providers = Plugin.load(config.plugins: _*)
  private val dataProvider = new AccumulatingDataProvider(providers)
  private val server = new ModbusServer(
    config,
    new SunspecDataMapper(Map(
      Sunspec.AccumulatedCurrentActivePower.address -> (() => dataProvider.currentProduction),
      Sunspec.TotalYieldWh.address -> (() => dataProvider.totalProduction)
    ))
  )

  server.start()
  TaskScheduling.findAll(providers:_*).foreach(_.start(config.pollInterval))

  sys.addShutdownHook {
    server.stop()
    server.awaitTermination()
  }

  System.gc()

  server.awaitTermination()
  TaskScheduling.findAll(providers:_*).foreach(_.stop())

  logger.info(s"Dobiss ModBus-TCP proxy - shut down")
}
