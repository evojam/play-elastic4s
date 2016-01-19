package com.evojam.play.elastic4s.client

import scala.concurrent.{ExecutionContext, Future}

import play.api.libs.json.Writes

trait ElasticSearchClient {

  /**
   * Index a document
   * https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-index_.html
   *
   * @param indexName name of the index to
   * @param doctype type of the document
   * @param id id to be assigned to the document
   * @param doc the document to be indexed
   * @param exc the execution context
   * @tparam T type of the document, must have an implicit Writes[T] available in scope
   * @return Future[Boolean] whose value depends upon the success of the indexing operation
   */
  def indexDocument[T](indexName: String, doctype: String, id: String, doc: T)(
    implicit w: Writes[T], exc: ExecutionContext): Future[Boolean]


  /**
   * Update a document
   * https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-update.html
   *
   * @param indexName name of the index to
   * @param doctype type of the document
   * @param id id to be assigned to the document
   * @param doc the document to be indexed
   * @param exc the execution context
   * @tparam T type of the document, must have an implicit Writes[T] available in scope
   * @return Future[Boolean] whose value depends upon the success of the update operation
   */
  // scalastyle:off method.length
  def updateDocument[T](indexName: String, doctype: String, id: String, doc: T)(
    implicit w: Writes[T], exc: ExecutionContext): Future[Boolean]

  /**
   * Remove a document
   * https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-delete.html
   *
   * @param indexName name of the index to
   * @param doctype type of the document
   * @param id id to be assigned to the document
   * @param exc the execution context
   * @return Future[Boolean] whose value depends upon the success of the remove operation
   */
  def removeDocument(indexName: String, doctype: String, id: String)(
    implicit exc: ExecutionContext): Future[Boolean]
}
