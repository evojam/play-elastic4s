package com.evojam.play.elastic4s.core

import play.api.libs.json.{Json, Reads}

import org.elasticsearch.action.get.GetResponse

package object crud {

  implicit class RichGetResponse(response: GetResponse) {

    def parseJson[T: Reads]: Option[T] =
      Some(response)
        .filter(_.isExists)
        .map(_.getSourceAsBytes)
        .map(Json.parse)
        .map(_.as[T])

  }

}
