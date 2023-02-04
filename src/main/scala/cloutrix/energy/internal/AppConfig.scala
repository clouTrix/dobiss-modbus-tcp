package cloutrix.energy.internal

import com.typesafe.config.{Config, ConfigObject, ConfigRenderOptions}
import com.typesafe.scalalogging.LazyLogging

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration
import scala.jdk.CollectionConverters.SetHasAsScala

class AppConfig (inner: Config) extends LazyLogging {
  logger.debug(s"inner config:\n${inner.root().render(ConfigRenderOptions.concise().setJson(true).setFormatted(true))}")

  import AppConfig._
  lazy val modbusTcpPort: Int = inner.getInt(MODBUS_TCP_PORT_CONFIG)
  lazy val pollInterval: FiniteDuration = FiniteDuration(
    length = inner.getDuration(POLL_INTERVAL_CONFIG).toMillis,
    unit = TimeUnit.MILLISECONDS
  )

  lazy val plugins: List[PluginDesc] = inner.getObject(PLUGINS_CONFIG).entrySet().asScala
    .map { e => e.getKey -> e.getValue }
    .map { case (name, cfg: ConfigObject) =>
      PluginDesc(
        name = name,
        config = cfg.toConfig.getConfig(PLUGIN_CONFIG_BLOCK),
        ctor = Plugin.loadClass(cfg.toConfig.getString(PLUGIN_CLASS_CONFIG))(_: Config)
      )
    }.toList
}

object AppConfig {
  private final val MODBUS_TCP_PORT_CONFIG: String = "modbus.tcp.port"
  private final val PLUGINS_CONFIG: String = "plugins"
  private final val PLUGIN_CLASS_CONFIG: String = "class"
  private final val PLUGIN_CONFIG_BLOCK: String = "config"
  private final val POLL_INTERVAL_CONFIG: String = "poll.interval"

  def load(config: Config): AppConfig = new AppConfig(config)
}
