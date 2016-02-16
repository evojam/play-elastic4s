package com.evojam.play.elastic4s.configuration

import org.elasticsearch.common.settings.Settings

import com.sksamuel.elastic4s.ElasticsearchClientUri

case class ClusterSetup(uri: ElasticsearchClientUri, settings: Settings = Settings.builder.build) {
}

