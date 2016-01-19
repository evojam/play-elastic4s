package com.evojam.play.elastic4s.core.search

import scala.concurrent.{ExecutionContext, Future}

import play.api.libs.json.{Json, Reads}

import org.elasticsearch.action.search.SearchResponse

import com.sksamuel.elastic4s.{ElasticClient, SearchDefinition}
import com.sksamuel.elastic4s.ElasticDsl._

final case class PreparedSearch(searchDefinition: SearchDefinition, underlying: ElasticClient) {

  private def collectHits[T: Reads](result: SearchResponse)(implicit exc: ExecutionContext): List[T] =
    result.getHits.getHits.toList
      .map(_.source)
      .map(Json.parse)
      .map(_.as[T])

  /**
    * Executes the SearchDefinition and parses the response from ElasticSearch with the play.api.libs.json.Reads in
    * implicit scope.
    *
    * @return Future[List[T]] representing parsed hits from the ElasticSearch SearchResponse.
    */
  def collect[T: Reads]()(implicit exc: ExecutionContext): Future[List[T]] =
    underlying.execute(searchDefinition)
      .map(collectHits[T])

}


