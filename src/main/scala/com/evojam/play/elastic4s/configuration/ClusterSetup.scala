package com.evojam.play.elastic4s.configuration

import com.sksamuel.elastic4s.ElasticsearchClientUri

import org.elasticsearch.common.settings.Settings

case class ClusterSetup(
  name: String,
  settings: Settings,
  uri: ElasticsearchClientUri,
  default: Boolean)
