package com.evojam.play.elastic4s.lifecycle

import javax.inject.{Singleton, Inject}

import scala.concurrent.Future

import play.api.inject.ApplicationLifecycle

import com.sksamuel.elastic4s.ElasticClient
import scala.collection.mutable

import com.evojam.play.elastic4s.configuration.ClusterSetup


@Singleton
private[elastic4s] class LifecycleElasticFactory @Inject() (lifecycle: ApplicationLifecycle) {
  private[this] val clients =  mutable.Map.empty[ClusterSetup, ElasticClient]

  private[this] def buildTransportClient(setup: ClusterSetup) = {
    val client = ElasticClient.transport(setup.settings, setup.uri)
    lifecycle.addStopHook(() => Future.successful {
      client.close() // FIXME: elastc4s .close() silently drops all exceptions
    })
    client
  }

  def getOrCreate(setup: ClusterSetup) = clients.getOrElseUpdate(setup, buildTransportClient(setup))

}
