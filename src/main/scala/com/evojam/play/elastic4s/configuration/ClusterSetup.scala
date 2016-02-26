package com.evojam.play.elastic4s.configuration

import org.elasticsearch.common.settings.Settings

import com.sksamuel.elastic4s.ElasticsearchClientUri

sealed trait ClusterSetup {
  val settings: Settings
}

case class RemoteClusterSetup(
  uri: ElasticsearchClientUri,
  settings: Settings = Settings.builder.build)
    extends ClusterSetup

case class LocalNodeSetup(settings: Settings = Settings.builder.build) extends ClusterSetup
