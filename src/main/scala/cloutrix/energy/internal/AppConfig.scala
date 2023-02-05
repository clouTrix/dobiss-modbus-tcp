package cloutrix.energy.internal

import com.typesafe.config._
import com.typesafe.scalalogging.LazyLogging

import java.net.URI
import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration
import scala.jdk.CollectionConverters.SetHasAsScala
import scala.util.Try

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

  def load(config: Config = ConfigFactory.load(), envs: Map[String, String] = sys.env): AppConfig = {
    var cfg = config
    pluginConfigFromEnv(config)(envs).foreach { case id -> c =>
      cfg = cfg.withValue(s"plugins.${id}", c.root())
    }

    new AppConfig(cfg)
  }

  /**
   * allow plugin configuration through environment variables
   *
   * plugin identifier (name) is taken from the key: PLUGIN_(name)
   * the value consists out of 3 parts:
   *   - type of inverter: refers to the key of an existing configuration (see: reference.conf)
   *   - ip-address
   *   - port [optional] (default: 80)
   *
   * example:
   *  PLUGIN_EnvoyS = envoy@10.0.0.1
   *  PLUGIN_Saj1   = saj@10.0.0.2:8080
   */
  private def pluginConfigFromEnv(config: Config)(envs: Map[String, String]): Map[String, Config] = {
    val keyToIdRegex = """^PLUGIN_(.*)$""".r
    val valueRegex = """^([A-Za-z0-9_]+)@([A-za-z0-9:.]+)$""".r

    def asURI(v: String): Option[URI] = Try(new URI("my://" + v)).toOption

    //TODO: optimize!
    envs
      .view
      .flatMap { case (k, v) => Some(k).collect { case keyToIdRegex(id) => id -> v } }
      .flatMap { case (id, v) => Some(v).collect { case valueRegex(typ, inetaddr) => id -> (typ, asURI(inetaddr)) } }
      .collect { case (id, (typ, Some(uri))) => id -> (typ, (uri.getHost -> uri.getPort)) }
      .map { case (id, (typ, (host, port))) => id -> (config.getConfig(typ), (host -> port)) }
      .map {
        case (id, (cfg, (host, port))) if port > 0 =>
          id -> cfg
            .withValue("config.host", ConfigValueFactory.fromAnyRef(host))
            .withValue("config.port", ConfigValueFactory.fromAnyRef(Int.box(port)))

        case (id, (cfg, (host, _))) =>
          id -> cfg
            .withValue("config.host", ConfigValueFactory.fromAnyRef(host))

      }.toMap
  }
}
