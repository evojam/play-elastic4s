package com.evojam.play.elastic4s.configuration

import play.api.{Logger, Configuration}

import org.elasticsearch.common.settings.Settings
import org.elasticsearch.common.settings.loader.JsonSettingsLoader

import com.sksamuel.elastic4s.ElasticsearchClientUri
import com.typesafe.config.ConfigRenderOptions

import com.evojam.play.elastic4s.Elastic4sConfigException

object ClusterSetupLoader {

  private val logger = Logger(getClass)

  val UriKey = "uri"

  lazy val loader = new JsonSettingsLoader(false)

  def isTransport(config: Configuration) = config.getString(UriKey).nonEmpty

  def uri(config: Configuration) = config.getString(UriKey)
    .map(ElasticsearchClientUri(_))
    .getOrElse(throw new Elastic4sConfigException("Configuration field uri is mandatory"))

  def settings(config: Configuration): Settings = {
    val s = Settings.builder()
      .put("client.transport.sniff", true) // Will discover other hosts by default
      .put(loader.load(config.underlying.root().render(ConfigRenderOptions.concise())))

    s.remove(UriKey)
    s.build()
  }

  def setup(config: Configuration): ClusterSetup =
    if (isTransport(config)) {
      RemoteClusterSetup(uri(config), settings(config))
    }
    else {
      LocalNodeSetup(settings(config))
    }

  def getClusterSetups(clustersConf: Configuration): Map[String, ClusterSetup] = {
    val clusterSetups = clustersConf.subKeys
      .map(key => key -> setup(clustersConf.getConfig(key).get))
      .toMap
    logger info s"Loaded configuration for following clusters: ${clusterSetups.keys.mkString(",")}"
    clusterSetups
  }

}
