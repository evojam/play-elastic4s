package com.evojam.play.elastic4s.configuration

import play.api.{Configuration, Logger}

import com.sksamuel.elastic4s.IndexType

import com.evojam.play.elastic4s.Elastic4sConfigException

object IndexTypeConfigurationLoader {

  private val logger = Logger(getClass)
  val IndexTypesKey = "indexTypes"

  private def indexTypesMapping(indexTypesConfig: Configuration): Seq[(String, IndexType)] = {

    def indexTypeBinding(name: String) = {
      val indexTypeConfig = indexTypesConfig.getConfig(name).get
      def getRequiredField(field: String) = {
        indexTypeConfig
          .getString(field)
          .getOrElse(throw new Elastic4sConfigException(s"$field field is required for indexType $name"))
      }

      val indexType = IndexType(getRequiredField("index"), getRequiredField("type"))
      logger.info(s"Binding IndexType $indexType with name $name")
      name -> indexType
    }

    indexTypesConfig.subKeys.toSeq.map(indexTypeBinding)
  }
  def mappings(elastic4sConfig: Configuration) = elastic4sConfig
    .getConfig(IndexTypesKey)
    .map(indexTypesMapping)
    .getOrElse(Seq.empty)
}
