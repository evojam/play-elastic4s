package com.evojam.play.elastic4s

import com.sksamuel.elastic4s.ElasticClient

import com.evojam.play.elastic4s.configuration.ClusterSetup

/**
  * PlayElasticFactory creates ES clients hooked to Play application lifecycle.
  *
  * All the [[com.sksamuel.elastic4s.ElasticClient]] instances returned by this factory
  * will automatically disconnect on Play shutdown.
  */
trait PlayElasticFactory extends (ClusterSetup => ElasticClient)
