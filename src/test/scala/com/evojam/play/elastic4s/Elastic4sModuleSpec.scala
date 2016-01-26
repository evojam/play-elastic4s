package com.evojam.play.elastic4s

import org.specs2.mutable.Specification

import play.api.Configuration

import com.evojam.play.elastic4s.configuration.IndexTypeConfigurationLoader

class Elastic4sModuleSpec extends Specification {

  lazy val module = new Elastic4sModule

  "buildSetup" should {

    "build multiple instance setups" in {

      val conf = Configuration.from(Map(
        "myCluster" -> Map(
          "default" -> true,
          "uri" -> "elasticsearch://10.10.0.1:9300",
          "cluster.name" -> "my-cluster"),
        "yourCluster" -> Map(
          "default" -> true,
          "uri" -> "elasticsearch://10.10.0.2:9300",
          "cluster.name" -> "your-cluster"),
        "indexTypes" -> Map(
          "book" -> Map(
            "index" -> "library",
            "type" -> "book"
          ),
          "author" -> Map(
            "index" -> "library",
            "type" -> "author"
      ))))

      module.buildSetup(conf) must haveLength(2)
      IndexTypeConfigurationLoader.mappings(conf) must haveLength(2)


    }
  }

}
