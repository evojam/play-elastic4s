package com.evojam.play.elastic4s.json

import play.api.libs.json.{Json, Reads}

import org.elasticsearch.action.get.GetResponse

class GetResponseWithJson(val response: GetResponse) extends AnyVal {
  def as[T: Reads] = Some(response)
      .filter(_.isExists)
    .map(r => Json.parse(r.getSourceAsBytes).as[T])
}

