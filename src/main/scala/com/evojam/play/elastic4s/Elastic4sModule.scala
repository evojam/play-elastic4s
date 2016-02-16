package com.evojam.play.elastic4s

import play.api.inject.Module
import play.api.{Configuration, Environment, Logger}

import org.elasticsearch.common.settings.Settings

import com.sksamuel.elastic4s.ElasticsearchClientUri

import com.evojam.play.elastic4s.configuration.{ClusterConfigBuilder, ClusterSetup}

class Elastic4sConfigException(msg: String) extends Exception(msg: String)

class Elastic4sModule extends Module {

  case class InstanceSetup(name: String, settings: Settings, uri: ElasticsearchClientUri, default: Boolean)

  val logger = Logger(getClass)

  val ConfigurationKey = "elastic4s"
  val ClustersKey = "clusters"

  def namedBindings(clusters: Iterable[(String, ClusterSetup)]) = clusters map {
    case (name, setup) => bind[ClusterSetup].qualifiedWith(name).toInstance(setup)
  }

  def defaultBinding(clusterSetup: ClusterSetup) = bind[ClusterSetup].toInstance(clusterSetup)

  override def bindings(environment: Environment, configuration: Configuration) = {

    val elastic4sConfiguration = configuration.getConfig(ConfigurationKey)
      .getOrElse(throw new Elastic4sConfigException("You should provide Elastic4s configuration when loading module"))

    val clustersSetup = ClusterConfigBuilder.getClusterSetups(elastic4sConfiguration.getConfig("clusters")
        .getOrElse(throw new Elastic4sConfigException("Missing clusters configuration")))

    val optDefaultBinding = Option(clustersSetup).filter(_.size == 1).map(t => defaultBinding(t.head._2))

    (namedBindings(clustersSetup) ++ optDefaultBinding).toSeq
  }
}
