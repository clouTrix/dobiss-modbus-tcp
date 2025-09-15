package envoy

import cloutrix.energy.envoy.EnvoyDataProvider
import com.typesafe.config.ConfigFactory
import org.scalatest.concurrent.Eventually
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.{BeforeAndAfterAll, Inside}

import scala.util.{Success, Try}

/**
 * Requires following ENV vars to be set
 *  - ENLIGHTEN_SERIAL
 *  - ENLIGHTEN_USERNAME
 *  - ENLIGHTEN_PASSWORD
 *
 * The tests will be ignored when these variables are not found.
 */
class EnphaseLoginTests  extends AnyWordSpec with Inside with Matchers with BeforeAndAfterAll with Eventually {
    private val config = Try(ConfigFactory.load("test-envoy-login.conf").getConfig("EnvoyS.config"))

    config.map(_.getString("username")) -> config.map(_.getString("password")) -> config.map(_.getString("serial")) match {
        case Success(u) -> Success(p) -> Success(s) => doTests(u, p, s)
        case _ =>
            "can login to Enlighten API with real credentials" is pending
            "can get JWT from login" is pending
    }

    def doTests(user: String, password: String, serial: String): Unit = {
        "can login to Enlighten API with real credentials" in {
            inside(EnvoyDataProvider.onManagementSession(user, password)(config.get)) {
                case Success(r) =>
                    println(r)
            }
        }

        "can get JWT from login" in {
            val sessionId = EnvoyDataProvider.onManagementSession(user, password)(config.get).get
            inside(EnvoyDataProvider.fetchJwt(sessionId, serial, user)(config.get)) {
                case Success(r) =>
                    println(r)
            }
        }
    }
}
