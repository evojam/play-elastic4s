package com.evojam.play.elastic4s.client

import scala.concurrent.{ExecutionContext, Future}

import play.api.libs.json.Writes

import com.sksamuel.elastic4s.{ElasticClient, IndexType, SearchDefinition}

import org.elasticsearch.action.bulk.BulkResponse

import com.evojam.play.elastic4s.core.search.PreparedSearch

trait ElasticSearchClient {

  /*
   *  Use to access the underlying [[com.sksamuel.elastic4s.ElasticClient]] instance.
   */
  val underlying: ElasticClient

  /**
   * Indexes a document.
   *
   * Performs an ES Index request. This creates a new document in ES.
   *
   * @see [[https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-index_.html]]
   *
   * @param indexType index name and document type
   * @param id id to be assigned to the document
   * @param doc the document to be indexed
   * @param exc the execution context
   * @tparam T type of the document, must have an implicit Writes[T] available in scope
   * @return Future[Boolean] resolved with `true` if the operation succeeded
   */
  @throws[NotAJsObjectException[_]]("if the document serializes to something that is not a Json object")
  def index[T: Writes](indexType: IndexType, id: String, doc: T)(implicit exc: ExecutionContext): Future[Boolean]

  /**
   * Indexes a document, assigning a new id.
   *
   * @see [[https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-index_.html]]
   *
   * @param indexType index name and document type
   * @param doc document contents
   * @param exc execution context
   * @tparam T type of the document with Writes[T] in implicit scope
   * @return Future[Boolean] resolved with `true` if the operation succeeded
   */
  @throws[NotAJsObjectException[_]]("if the document serializes to something that is not a Json object")
  def index[T: Writes](indexType: IndexType, doc: T)(implicit exc: ExecutionContext): Future[Boolean]

  /**
   * Updates a document.
   *
   * Performs an ES Update request, overriding the previously stored document with the given id.
   *
   * @see [[https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-update.html]]
   *
   * @param indexType index name and document type
   * @param id id of the document to be updated
   * @param doc the new document contents
   * @param exc the execution context
   * @param upsert if `true` the document will be created if it doesn't exist
   * @tparam T type of the document, must have an implicit Writes[T] available in scope
   * @return Future[Boolean] resolved with `true` if the operation succeeded
   */
  @throws[NotAJsObjectException[_]]("if the document serializes to something that is not a Json object")
  def update[T: Writes](indexType: IndexType, id: String, doc: T, upsert: Boolean = false)
      (implicit exc: ExecutionContext): Future[Boolean]
  /**
   * Removes a document with the given ID.
   *
   * @see [[https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-delete.html]]
   *
   * @param indexType index name and document type
   * @param id id to be assigned to the document
   * @param exc the execution context
   * @return Future[Boolean] resolved with `true` if the document was deleted successfully, `false` if it
   *         couldn't be found.
   */
  def remove(indexType: IndexType, id: String)(implicit exc: ExecutionContext): Future[Boolean]

  /**
   * Prepares a search query for execution.
   *
   * @param searchDef the search definition constructed using elastic4s DSL
   * @return a [[com.evojam.play.elastic4s.core.search.PreparedSearch]] instance encapsulating the query
   * @example
   * {{{
   *  import play.api.libs.Json
   *
   *  case class City(name: String, population: Long)
   *  object City {
   *    implicit val format = Json.format[City]
   *  }
   *
   *  val elastic: ElasticClient = ...
   *  val myQuery: SearchDefinition = search in "places" -> "cities" query "London"
   *  val result: Future[List[City]] = elastic.search(myQuery).collect[City]
   * }}}
   */
  def search(searchDef: SearchDefinition): PreparedSearch

  /**
   * Indexes multiple documents into a single index using ES Bulk API.
   *
   * It is possible that only a subset of the documents will be indexed properly - ES will simply
   * accept all the valid documents and drop the malformed ones. Inspect the resulting
   * [[org.elasticsearch.action.bulk.BulkResponse]] for details on which documents were indexed successfully.
   *
   * @see [[https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-bulk.html]]
   *
   * @param indexType index name and document type
   * @param documents documents to be indexed
   * @tparam T type of the documents with Writes[T] available in the implicit scope
   * @return the original response from ES
   */
  @throws[NotAJsObjectException[_]]("if some of the documents serialize to something that is not a Json object")
  def bulkIndex[T: Writes](indexType: IndexType, documents: Iterable[T]): Future[BulkResponse]
}


