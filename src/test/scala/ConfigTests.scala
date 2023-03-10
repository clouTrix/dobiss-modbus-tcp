import cloutrix.energy.internal.AppConfig
import com.typesafe.config.ConfigFactory
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class ConfigTests extends AnyWordSpec with Matchers {

  "configuration can be loaded" in {
    val config = AppConfig.load(ConfigFactory.load())

    config.modbusTcpPort should not be 0
    config.plugins should not be (empty)
  }
}
