package cloutrix.energy.internal

trait DataProviderCache extends DataProvider {
  private var cp: Option[Int] = None
  private var tp: Option[Int] = None

  def cache(currentProduction: Option[Int] = None, totalProduction: Option[Int] = None): Unit = {
    currentProduction.map(Some(_)).foreach(cp = _)
    totalProduction.map(Some(_)).foreach(tp = _)
  }

  override def currentProduction: Int = cp.getOrElse( throw new IllegalStateException("production data not yet available") )

  override def totalProduction: Int = tp.getOrElse( throw new IllegalStateException("production data not yet available") )
}
