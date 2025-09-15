package cloutrix.energy.internal

class AccumulatingDataProvider(providers: Seq[DataProvider]) extends DataProvider {
    override def currentProduction: Int = providers.map(_.currentProduction).sum
    override def totalProduction: Int = providers.map(_.totalProduction).sum
}
