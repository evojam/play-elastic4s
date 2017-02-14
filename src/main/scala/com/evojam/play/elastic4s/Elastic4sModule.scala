package com.evojam.play.elastic4s

import play.api.inject.Module
import play.api.{Configuration, Environment, Logger}

import org.elasticsearch.common.settings.Settings

import com.sksamuel.elastic4s.{IndexAndType, ElasticsearchClientUri}

import com.evojam.play.elastic4s.configuration.{IndexAndTypeConfigLoader, ClusterSetupLoader, ClusterSetup}
import com.evojam.play.elastic4s.lifecycle.PlayElasticFactoryImpl

class Elastic4sConfigException(msg: String) extends Exception(msg: String)

class Elastic4sModule extends Module {

  case class InstanceSetup(name: String, settings: Settings, uri: ElasticsearchClientUri, default: Boolean)

  val logger = Logger(getClass)

  val ConfigurationKey = "elastic4s"
  val ClustersKey = "clusters"
  val IdxTypesKey = "indexAndTypes"

  def namedBindings(clusters: Iterable[(String, ClusterSetup)]) = clusters map {
    case (name, setup) => bind[ClusterSetup].qualifiedWith(name).toInstance(setup)
  }

  def defaultBinding(clusterSetup: ClusterSetup) = bind[ClusterSetup].toInstance(clusterSetup)

  def factoryBindings = Seq(
    bind[PlayElasticFactory].to[PlayElasticFactoryImpl]
  )

  def indexTypesBindings(indexTypes: Map[String, IndexAndType]) = indexTypes map {
    case (name, idxType) => bind[IndexAndType].qualifiedWith(name).toInstance(idxType)
  }

  override def bindings(environment: Environment, configuration: Configuration) = {

    val elastic4sConfiguration = configuration.getConfig(ConfigurationKey)
      .getOrElse(throw new Elastic4sConfigException("You should provide Elastic4s configuration when loading module"))

    val clustersSetup = ClusterSetupLoader.getClusterSetups(elastic4sConfiguration.getConfig(ClustersKey)
      .getOrElse(throw new Elastic4sConfigException("Missing clusters configuration")))

    val idxTypeBindings = elastic4sConfiguration.getConfig(IdxTypesKey)
      .map(IndexAndTypeConfigLoader.getIdxTypesConfig)
      .toSeq.flatMap(indexTypesBindings)

    val optDefaultBinding = Option(clustersSetup).filter(_.size == 1).map(t => defaultBinding(t.head._2))

    logger.debug(
      s"Provide bindings for ES configuration, cluster setup, type bindings and default ES client ${optDefaultBinding}"
    )

    factoryBindings ++ namedBindings(clustersSetup) ++ optDefaultBinding ++ idxTypeBindings
  }
}
