package com.evojam.play.elastic4s.core.crud

import scala.concurrent.{ExecutionContext, Future}

import play.api.libs.json.Reads

import com.sksamuel.elastic4s.{ElasticClient, GetDefinition, GetDsl}

final case class PreparedGet(getDefinition: GetDefinition, underlying: ElasticClient) extends GetDsl {

  /**
    * Execute the [[GetDefinition]] and parse using the [[play.api.libs.json.Reads]] in scope
    * @param exc the execution context
    * @tparam T the document to be fetched
    * @return Future[Option[T]] that resolves to Some[T] when the document is found.
    */
  def collect[T: Reads](implicit exc: ExecutionContext): Future[Option[T]] =
    underlying.execute(getDefinition)
      .map(_.parseJson[T])

}
