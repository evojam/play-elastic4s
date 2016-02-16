package com.evojam.play.elastic4s

import javax.inject.Inject

import com.evojam.play.elastic4s.configuration.ClusterSetup
import com.evojam.play.elastic4s.lifecycle.LifecycleElasticFactory

trait PlayElasticFactory {
  @Inject() lazy val lifecycleElasticFactory: LifecycleElasticFactory = null // this will actually get injected

  def getElasticClient(setup: ClusterSetup) = lifecycleElasticFactory.getOrCreate(setup)

}
