package com.evojam.play.elastic4s.core.crud

import scala.concurrent.ExecutionContext

import play.api.libs.json.{Json, Reads}

import com.sksamuel.elastic4s._
import org.elasticsearch.action.get.GetResponse

final case class PreparedMultiGet(queries: Iterable[GetDefinition], underlying: ElasticClient) extends ElasticDsl {

  /**
    * Execute the multiget query using the many [[GetDefinition]] provided.
    * @param exc the execution context
    * @tparam T the type of documents to be fetched
    * @return Future[List[T]] resolved with the documents retrieved. Those not found are skipped.
    */
  def collect[T: Reads](implicit exc: ExecutionContext) =
    underlying.execute(multiget(queries))
      .map(_.getResponses.toList)
      .map(_.map(_.getResponse)
        .flatMap(_.parseJson[T]))

}

object PreparedMultiGet extends ElasticDsl {

  def apply(ids: Iterable[String], docType: IndexType, underlying: ElasticClient): PreparedMultiGet =
    PreparedMultiGet(ids.map(get id _ from docType), underlying)

}
