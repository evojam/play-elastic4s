package com.evojam.play.elastic4s.configuration

import play.api.Configuration

import org.elasticsearch.common.settings.Settings
import org.elasticsearch.common.settings.loader.JsonSettingsLoader

import com.sksamuel.elastic4s.ElasticsearchClientUri
import com.typesafe.config.ConfigRenderOptions

import com.evojam.play.elastic4s.Elastic4sConfigException

object ClusterConfigBuilder {

  val UriKey = "uri"
  lazy val loader = new JsonSettingsLoader()


  def uri(config: Configuration) = config.getString(UriKey)
    .map(ElasticsearchClientUri(_))
    .getOrElse(throw new Elastic4sConfigException("Configuration field uri is mandatory"))

  def settings(config: Configuration): Settings = {
    Settings.settingsBuilder()
      .put("client.transport.sniff", true) // Default behaviour for us
      .put(loader.load(config.underlying.root().render(ConfigRenderOptions.concise())))
      .build()
  }

  def setup(config: Configuration): ClusterSetup = ClusterSetup(uri(config), settings(config))

  def getClusterSetups(clustersConf: Configuration): Map[String, ClusterSetup] = {
    clustersConf.keys
      .map(key => key -> setup(clustersConf.getConfig(key).get))
      .toMap
  }

}
