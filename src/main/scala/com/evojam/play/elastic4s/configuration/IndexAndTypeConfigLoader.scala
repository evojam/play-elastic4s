package com.evojam.play.elastic4s.configuration

import play.api.{Logger, Configuration}

import com.sksamuel.elastic4s.IndexAndType

import com.evojam.play.elastic4s.Elastic4sConfigException

object IndexAndTypeConfigLoader {
  private[this] val logger = Logger(getClass)

  def indexAndType(idxTypeConfig: Configuration) = {
    val index = idxTypeConfig.getString("index")
        .getOrElse(throw new Elastic4sConfigException("Configuration field index is required"))
    val typ = idxTypeConfig.getString("type")
          .getOrElse(throw new Elastic4sConfigException("Configuration field type is required"))
    IndexAndType(index, typ)
  }

  def getIdxTypesConfig(idxTypesConfig: Configuration) = {
    val idxTypes = idxTypesConfig.subKeys
      .map(key => key -> indexAndType(idxTypesConfig.getConfig(key).get))
      .toMap
    logger info s"Loaded following index/type configurations: ${idxTypes.keys.mkString(",")}"
    idxTypes
  }

}
