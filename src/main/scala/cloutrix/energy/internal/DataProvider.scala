package cloutrix.energy.internal

trait DataProvider {
  def currentProduction: Int    // unsigned int  (**MUST** be positive)
  def totalProduction: Int      // unsigned int  (**MUST** be positive)
}
