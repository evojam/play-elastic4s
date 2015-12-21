package com.evojam.play.elastic4s.client

import scala.concurrent.{Future, ExecutionContext}
import scala.concurrent.Future.successful
import scala.language.implicitConversions

import org.elasticsearch.action.ActionResponse
import org.elasticsearch.action.search.SearchResponse

import play.api.Logger
import play.api.libs.json._

import com.google.inject.Inject
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.source.DocumentSource
import com.sksamuel.elastic4s.{ElasticClient, SearchDefinition}

class ElasticSearchClient @Inject() (val underlying: ElasticClient) {

  val logger = Logger(getClass)

  private case class JsValueSource(js: JsValue) extends DocumentSource {
    override def json = js.toString()
  }

  private implicit def toSource[T <: JsValue](js: T): DocumentSource = JsValueSource(js)

  private def getHeaders[R <: ActionResponse](response: R) =
    Option(response.getHeaders)
      .map(_.toString)
      .getOrElse("<null>")

  private def executeInsert(indexName: String, doctype: String, id: String, in: DocumentSource)
    (implicit exc: ExecutionContext) =
    underlying.execute {
      logger.debug(s"Index into index=$indexName type=$doctype doc=${in.json}")
      index.into(indexName -> doctype)
        .doc(in)
        .id(id)
    }

  /**
   * Execute a search definition
   *
   * @param searchDef the search definition to execute
   * @return SearchResponse received from ElasticSearch
   */
  def search(searchDef: SearchDefinition)(implicit exc: ExecutionContext): Future[SearchResponse] =
    underlying.execute(searchDef)

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
  def indexDocument[T](indexName: String, doctype: String, id: String, doc: T)
    (implicit w: Writes[T], exc: ExecutionContext): Future[Boolean] = {
    require(indexName != null, "indexName cannot be null")
    require(doctype != null, "doctype cannot be null")
    require(id != null, "id cannot be null")
    require(doc != null, "doc cannot be null")

    w.writes(doc) match {
      case json: JsObject =>
        executeInsert(indexName, doctype, id, json).map(Option(_))
          .map {
            case Some(response) if response.isCreated =>
              logger.info("Document has been indexed (create), retrieving headers= " + getHeaders(response))
              true
            case Some(response) if !response.isCreated =>
              logger.info("Document has been indexed (update), retrieving headers= " + getHeaders(response))
              true
            case None =>
              logger.error("Elastic execute result IndexResponse is null, I treat it as an error but can do nothing")
              false
          }
      case _ =>
        logger.error("Refuse to index document that is not a JsObject")
        successful(false)
    }
  }

  private def executeUpdate(indexName: String, doctype: String, id: String, in: DocumentSource)
    (implicit exc: ExecutionContext) =
    underlying.execute {
      logger.debug(s"Update document index=$indexName type=$doctype id=$id doc=${in.json}")
      update
        .id(id)
        .in(indexName -> doctype)
        .doc(in)
    }

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
  def updateDocument[T](indexName: String, doctype: String, id: String, doc: T)
    (implicit w: Writes[T], exc: ExecutionContext): Future[Boolean] = {
    require(indexName != null, "indexName cannot be null")
    require(doctype != null, "doctype cannot be null")
    require(id != null, "id cannot be null")
    require(doc != null, "doc cannot be null")

    w.writes(doc) match {
      case json: JsObject =>
        executeUpdate(indexName, doctype, id, json).map(Option(_))
          .map {
            case Some(response) if response.isCreated =>
              logger.info("Document has been updated, retrieving headers=" + getHeaders(response))
              true
            case Some(response) =>
              logger.info("Document has been updated, retrieving headers=" + getHeaders(response))
              true
            case None =>
              logger.error("Elastic execute update result IndexResponse is null, " +
                           "Treating it as an error but can do nothing")
              false
          }
      case _ =>
        logger.error("Refuse to update document that is not a JsObject")
        successful(false)
    }
  }
  // scalastyle:on

  private def executeRemove(indexName: String, doctype: String, id: String)(implicit exc: ExecutionContext) =
    underlying.execute {
      logger.debug(s"Remove document from index=$indexName type=$doctype id=$id")
      delete
        .id(id)
        .from(indexName -> doctype)
    }

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
  def removeDocument(indexName: String, doctype: String, id: String)
    (implicit exc: ExecutionContext): Future[Boolean] = {
      require(indexName != null, "indexName cannot be null")
      require(doctype != null, "doctype cannot be null")
      require(id != null, "id cannot be null")

      executeRemove(indexName, doctype, id).map {
        case ok if ok.isFound => true
        case _ =>
          logger.warn(s"Document in index=$indexName type=$doctype id=$id not found")
          false
      }
    }
}
