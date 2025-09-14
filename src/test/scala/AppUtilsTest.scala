import cloutrix.energy.internal.Utils
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class AppUtilsTest extends AnyWordSpec with Matchers {

    "positiveOrDefault" should {
        "return the input value" when {
            "input > 0" in {
                Utils.positiveOrElse(666)(1) should be (1)
            }

            "input == Int.MaxValue" in {
                Utils.positiveOrElse(666)(Int.MaxValue) should be(Int.MaxValue)
            }
        }

        "return the default value" when {
            "input < 0" in {
                Utils.positiveOrElse(666)(-1) should be (666)
            }

            "input == Int.MinValue" in {
                Utils.positiveOrElse(666)(Int.MinValue) should be(666)
            }

            "input == 0" in {
                Utils.positiveOrElse(666)(0) should be (666)
            }

            "input == null" in {
                Utils.positiveOrElse(666)(null.asInstanceOf[Int]) should be (666)
            }
        }
    }

}
