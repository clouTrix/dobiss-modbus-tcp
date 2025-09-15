package cloutrix.energy.internal

import com.typesafe.config.Config
import com.typesafe.scalalogging.StrictLogging

case class PluginDesc(name: String, config: Config, ctor: Config => DataProvider)

object Plugin extends StrictLogging {
    def loadClass(clazz: String): Config => DataProvider = {
        val ctor = Class.forName(clazz).getDeclaredConstructor(classOf[Config])
        logger.info(s"loaded plugin - class: ${ctor.getName}")

        config => ctor
            .newInstance(config)
            .asInstanceOf[DataProvider]
    }

    def load(pluginDescs: PluginDesc*): Seq[DataProvider] =
        pluginDescs
            .tapEach(ps => logger.info(s"activate plugin - name: ${ps.name}"))
            .map(desc => desc.ctor(desc.config))
}
