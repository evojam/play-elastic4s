package com.evojam.play.elastic4s.configuration

import play.api.{Logger, Configuration}
import com.sksamuel.elastic4s.ElasticsearchClientUri
import com.typesafe.config.ConfigRenderOptions

import org.elasticsearch.common.settings.{ImmutableSettings, Settings}
import org.elasticsearch.common.settings.loader.JsonSettingsLoader

import com.evojam.play.elastic4s.Elastic4sConfigException

object ClusterConfigurationLoader {

  private val UriKey = "uri"
  private val loader = new JsonSettingsLoader()
  private val logger = Logger(getClass)

  private def settings(config: Configuration): Settings =
    ImmutableSettings.settingsBuilder()
      .put("client.transport.sniff", true) // Default behaviour for us
      .put(loader.load(config.underlying.root().render(ConfigRenderOptions.concise())))
      .build()

  private def uri(clusterConfig: Configuration) = clusterConfig.getString(UriKey)
    .map(ElasticsearchClientUri(_))
    .getOrElse(throw new Elastic4sConfigException(s"Configuration field $UriKey is mandatory"))

  def clusterSetup(elastic4sConfig: Configuration)(name: String) = {
    val clusterConfig = elastic4sConfig.getConfig(name).get
    logger.info(s"Loading setup for cluster $name")
    ClusterSetup(
      name,
      settings(clusterConfig),
      uri(clusterConfig),
      clusterConfig.getBoolean("default").getOrElse(false))
  }
}


