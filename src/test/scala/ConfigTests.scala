import cloutrix.energy.internal.AppConfig
import com.typesafe.config.{ConfigException, ConfigFactory}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class ConfigTests extends AnyWordSpec with Matchers {

    "configuration can be loaded" in {
        val config = AppConfig.load(ConfigFactory.load("test-app.conf"))

        config.modbusTcpPort should not be 0
        config.plugins should not be (empty)

        config.totalProductionCorrection should be (0)
        config.immediateProductionCorrection should be (0)
    }

    "plugin configuration can be done through environment variables" in {
        val config = AppConfig.load(envs = Map(
            "PLUGIN_xxx" -> "envoy@10.0.0.1",
            "PLUGIN_yyy" -> "saj@10.0.0.2:8080"
        ))

        config.plugins.map(_.name) should contain theSameElementsAs Seq(
            "xxx", "yyy"
        )
    }

    "loading plugin of unsupported type should fail" in {
        intercept[ConfigException.Missing] (AppConfig.load(envs = Map("PLUGIN_xxx" -> "unknown@10.0.0.1")) )
    }
}
