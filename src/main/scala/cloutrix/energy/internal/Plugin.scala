package cloutrix.energy.internal

import com.typesafe.config.Config

case class PluginDesc(name: String, config: Config, ctor: Config => DataProvider)

object Plugin {
  def loadClass(clazz: String)(config: Config): DataProvider = Class.forName(clazz)
                                                                    .getDeclaredConstructor(classOf[Config])
                                                                    .newInstance(config)
                                                                    .asInstanceOf[DataProvider]

  def load(pluginDescs: PluginDesc*): Seq[DataProvider] = pluginDescs.map(desc => desc.ctor(desc.config))
}
