package com.evojam.play.elastic4s

import org.elasticsearch.common.settings.loader.JsonSettingsLoader
import org.elasticsearch.common.settings.{ImmutableSettings, Settings}

import play.api.inject.{Binding, Module}
import play.api.{Configuration, Environment, Logger}

import com.sksamuel.elastic4s.{ElasticClient, ElasticsearchClientUri}
import com.typesafe.config.ConfigRenderOptions

class Elastic4sConfigException(msg: String) extends Exception(msg: String)

class Elastic4sModule extends Module {

  case class InstanceSetup(name: String, settings: Settings, uri: ElasticsearchClientUri, default: Boolean)

  val logger = Logger(getClass)

  val ConfigurationKey = "elastic4s"
  val UriKey = "uri"

  def uri(config: Configuration) = config.getString(UriKey)
    .map(ElasticsearchClientUri(_))
    .getOrElse(throw new Elastic4sConfigException("Configuration field uri is mandatory"))

  def settings(config: Configuration): Settings = {

    val loader = new JsonSettingsLoader() // Workaround to avoid code dups

    ImmutableSettings.settingsBuilder()
      .put("client.transport.sniff", true) // Default behaviour for us
      .put(loader.load(config.underlying.root().render(ConfigRenderOptions.concise())))
      .build()
  }

  def buildSetup(in: Configuration): Seq[InstanceSetup] =
    in.subKeys.flatMap(name => in.getConfig(name).map(name -> _)).toSeq.map {
      case (name, config) =>
        logger.info(s"Provide ElasticClient with configuration name=$name")
        InstanceSetup(name, settings(config), uri(config), config.getBoolean("default").getOrElse(false))
    }

  def namedBinding(name: String, instance: ElasticClient) =
    bind[ElasticClient].qualifiedWith(name).toInstance(instance)

  def defaultBinding(instance: ElasticClient) =
    bind[ElasticClient].toInstance(instance)

  def bindings(instance: ElasticClient, setup: InstanceSetup) =
    if (setup.default) {
      namedBinding(setup.name, instance) :: defaultBinding(instance) :: Nil
    } else {
      namedBinding(setup.name, instance) :: Nil
    }

  def bindings(setup: InstanceSetup): Seq[Binding[_]] =
    bindings(ElasticClient.remote(setup.settings, setup.uri), setup)

  override def bindings(environment: Environment, configuration: Configuration) = {

    val elastic4sConfiguration = configuration.getConfig(ConfigurationKey)
      .getOrElse(throw new Elastic4sConfigException("You should provide Elastic4s configuration when loading module"))

    val instancesSetup = buildSetup(elastic4sConfiguration)

    if (instancesSetup.count(_.default == true) > 1) {
      throw new Elastic4sConfigException("Cannot bind multiple default instances of ElasticClient")
    }

    instancesSetup.flatMap(bindings)
  }
}
