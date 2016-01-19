package com.evojam.play.elastic4s.client

import scala.concurrent.{ExecutionContext, Future}

import play.api.libs.json.Writes

import com.sksamuel.elastic4s.SearchDefinition

import com.evojam.play.elastic4s.core.search.PreparedSearch

trait ElasticSearchClient {

  /**
   * Indexes or updates a document.
   *
   * Performs an ES Index request. If a document with given ID exists, it will be overwritten.
   *
   * @see [[https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-index_.html]]
   *
   * @param indexName name of the index
   * @param doctype type of the document
   * @param id id to be assigned to the document
   * @param doc the document to be indexed
   * @param exc the execution context
   * @tparam T type of the document, must have an implicit Writes[T] available in scope
   * @return Future[Boolean] resolved with `true` if the operation succeeded
   */
  def upsert[T: Writes](indexName: String, doctype: String, id: String, doc: T)
      (implicit exc: ExecutionContext): Future[Boolean]

  /**
   * Removes a document with the given ID.
   *
   * @see [[https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-delete.html]]
   *
   * @param indexName name of the index to
   * @param doctype type of the document
   * @param id id to be assigned to the document
   * @param exc the execution context
   * @return Future[Boolean] resolved with `true` if the document was deleted successfully, `false` if it
   *         couldn't be found.
   */
  def remove(indexName: String, doctype: String, id: String)(implicit exc: ExecutionContext): Future[Boolean]

  /**
   * Prepares a search query for execution.
   *
   * @param searchDef the search definition constructed using elastic4s DSL
   * @return a [[com.evojam.play.elastic4s.core.search.PreparedSearch]] instance encapsulating the query
   * @example
   * {{{
   * import play.api.libs.Json
   *
   * case class City(name: String, population: Long)
   * object City {
   *   implicit val format = Json.format[City]
   * }
   *
   * val elastic: ElasticClient = ...
   * val myQuery: SearchDefinition = search in "places" -> "cities" query "London"
   * val result: Future[List[City]] = elastic.search(myQuery).collect[City]
   * }}}
   */
  def search(searchDef: SearchDefinition): PreparedSearch
}
