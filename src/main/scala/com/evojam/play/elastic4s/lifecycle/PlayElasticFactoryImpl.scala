package com.evojam.play.elastic4s.lifecycle

import javax.inject.{Inject, Singleton}

import scala.collection.mutable
import scala.concurrent.Future

import play.api.inject.ApplicationLifecycle

import org.elasticsearch.common.settings.Settings

import com.sksamuel.elastic4s.{ElasticsearchClientUri, ElasticClient}

import com.evojam.play.elastic4s.PlayElasticFactory
import com.evojam.play.elastic4s.configuration.{LocalNodeSetup, RemoteClusterSetup, ClusterSetup}


@Singleton
class PlayElasticFactoryImpl @Inject()(lifecycle: ApplicationLifecycle) extends PlayElasticFactory {
  private[this] val clients =  mutable.Map.empty[ClusterSetup, ElasticClient]

  private[this] def withStopHook(client: ElasticClient) = {
    lifecycle.addStopHook(() => Future.successful {
      client.close() // FIXME: elastc4s .close() silently drops all exceptions
    })
    client
  }

  def apply(cs: ClusterSetup) = clients.getOrElseUpdate(cs, withStopHook(cs match {
    case RemoteClusterSetup(uri, settings) => ElasticClient.transport(settings, uri)
    case LocalNodeSetup(settings) => ElasticClient.local(settings)
  }))

}



