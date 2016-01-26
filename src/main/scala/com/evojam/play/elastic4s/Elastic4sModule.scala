package com.evojam.play.elastic4s

import play.api.{Configuration, Environment, Logger}
import play.api.inject.{Binding, Module}

import com.sksamuel.elastic4s.{ElasticClient, IndexType}

import com.evojam.play.elastic4s.client.{ElasticSearchClient, ElasticSearchClientImpl}
import com.evojam.play.elastic4s.configuration.{IndexTypeConfigurationLoader, ClusterConfigurationLoader, ClusterSetup}

class Elastic4sConfigException(msg: String) extends Exception(msg: String)

class Elastic4sModule extends Module {

  val logger = Logger(getClass)

  val ConfigurationKey = "elastic4s"

  def buildSetup(in: Configuration): Seq[ClusterSetup] =
    in.subKeys
      .filterNot(_ == IndexTypeConfigurationLoader.IndexTypesKey)
      .toSeq
      .map(ClusterConfigurationLoader.clusterSetup(in))

  def namedBinding(name: String, instance: ElasticClient) =
    bind[ElasticSearchClient].qualifiedWith(name).toInstance(new ElasticSearchClientImpl(instance))

  def defaultBinding(instance: ElasticClient) =
    bind[ElasticSearchClient].toInstance(new ElasticSearchClientImpl(instance))

  def bindings(instance: ElasticClient, setup: ClusterSetup) =
    if (setup.default) {
      namedBinding(setup.name, instance) :: defaultBinding(instance) :: Nil
    } else {
      namedBinding(setup.name, instance) :: Nil
    }

  def bindings(setup: ClusterSetup): Seq[Binding[_]] =
    bindings(ElasticClient.remote(setup.settings, setup.uri), setup)

  def indexTypeBinding(mapping: (String, IndexType)) = mapping match {
    case (name, indexType) => bind[IndexType].qualifiedWith(name).toInstance(indexType)
  }

  override def bindings(environment: Environment, configuration: Configuration) = {

    val elastic4sConfiguration = configuration.getConfig(ConfigurationKey)
      .getOrElse(throw new Elastic4sConfigException("You should provide Elastic4s configuration when loading module"))
    val clusterSetup = buildSetup(elastic4sConfiguration)
    if (clusterSetup.count(_.default == true) > 1) {
      throw new Elastic4sConfigException("Cannot bind multiple default ES clusters")
    }
    val indexTypes = IndexTypeConfigurationLoader.mappings(elastic4sConfiguration)
    clusterSetup.flatMap(bindings) ++ indexTypes.map(indexTypeBinding)
  }

}
