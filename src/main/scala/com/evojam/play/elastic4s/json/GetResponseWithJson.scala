package com.evojam.play.elastic4s.json

import play.api.libs.json.{Json, Reads}

import org.elasticsearch.action.get.GetResponse

/**
  * Extends [[com.sksamuel.elastic4s.RichGetResponse]] with deserialization based on Play JSON formatters.
  *
  * Suggested usage via implicit conversion provided by [[com.evojam.play.elastic4s.PlayElasticJsonSupport]] mixin.
  */
class GetResponseWithJson(val response: GetResponse) extends AnyVal {
  /**
    * Tries to convert the result into T. Throws an exception if result is nonempty but cannot be converted.
    * @tparam T desired type of result. Must have a [[play.api.libs.json.Reads[T]]] available in implicit scope.
    * @return Parsed instance or None if the source does not exist.
    */
  def as[T: Reads] = Some(response)
      .filter(_.isExists)
    .map(r => Json.parse(r.getSourceAsBytes).as[T])
}

