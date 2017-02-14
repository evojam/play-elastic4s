package com.evojam.play.elastic4s.lifecycle

import javax.inject.{ Inject, Singleton }

import scala.collection.mutable
import scala.concurrent.Future

import com.sksamuel.elastic4s.TcpClient
import com.sksamuel.elastic4s.embedded.LocalNode
import play.api.inject.ApplicationLifecycle

import com.evojam.play.elastic4s.PlayElasticFactory
import com.evojam.play.elastic4s.configuration.{ ClusterSetup, LocalNodeSetup, RemoteClusterSetup }


@Singleton
class PlayElasticFactoryImpl @Inject()(lifecycle: ApplicationLifecycle) extends PlayElasticFactory {
  private[this] val clients = mutable.Map.empty[ClusterSetup, TcpClient]

  private[this] def withStopHook(client: TcpClient) = {
    lifecycle.addStopHook(() => Future.successful {
      client.close() // FIXME: elastc4s .close() silently drops all exceptions
    })
    client
  }

  def apply(cs: ClusterSetup) = clients.getOrElseUpdate(cs, withStopHook(cs match {
    case RemoteClusterSetup(uri, settings) => TcpClient.transport(settings, uri)
    case LocalNodeSetup(settings) => LocalNode(settings).elastic4sclient(true)
  }))

}



