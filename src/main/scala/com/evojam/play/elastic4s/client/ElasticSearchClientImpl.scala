package com.evojam.play.elastic4s.client

import scala.concurrent.{ExecutionContext, Future}
import scala.language.implicitConversions

import play.api.Logger
import play.api.libs.json._

import org.elasticsearch.action.ActionResponse
import org.elasticsearch.action.index.IndexResponse

import com.google.inject.Inject
import com.sksamuel.elastic4s.{ElasticClient, SearchDefinition}
import com.sksamuel.elastic4s.ElasticDsl.{index => elastic4sindex, _}
import com.sksamuel.elastic4s.source.{DocumentSource, JsonDocumentSource}

import com.evojam.play.elastic4s.core.search.PreparedSearch

class ElasticSearchClientImpl @Inject() (val client: ElasticClient) extends ElasticSearchClient {

  private[this] val logger = Logger(getClass)

  /**
   * Extracts headers from ES response for logging purposes.
   * @param response ElasticSearch response
   * @return String with response headers or "&lt;null&gt;"
   */
  private def getHeaderString[R <: ActionResponse](response: R) =
    Option(response.getHeaders)
      .map(_.toString)
      .getOrElse("<null>")

  /**
   * Executes an index operation via the underlying ES client.
   *
   * If a document with the same id already exists, it will be overwritten.
   *
   * @param indexName name of the ES index
   * @param doctype type of ES document to be indexed
   * @param id id of the document
   * @param in the document to be indexed
   * @param exc the concurrent execution context
   * @return
   */
  private def executeIndex(indexName: String, doctype: String, id: String, in: DocumentSource)
      (implicit exc: ExecutionContext): Future[Option[IndexResponse]] =
    client.execute {
      logger.debug(s"Index into index=$indexName type=$doctype doc=${in.json}")
      elastic4sindex.into(indexName -> doctype)
        .doc(in)
        .id(id)
    } map Option.apply

  def search(searchDef: SearchDefinition): PreparedSearch =
    PreparedSearch(searchDef, client)


  def index[T: Writes](indexName: String, doctype: String, id: String, doc: T)
      (implicit exc: ExecutionContext): Future[Boolean] = {
    require(indexName != null, "indexName cannot be null")
    require(doctype != null, "doctype cannot be null")
    require(id != null, "id cannot be null")
    require(doc != null, "doc cannot be null")

    Json.toJson(doc) match {
      case json: JsObject =>
        executeIndex(indexName, doctype, id, JsonDocumentSource(Json.stringify(json))).map {
          case Some(response) if response.isCreated =>
            logger.info("Document has been indexed (create), retrieving headers= " + getHeaderString(response))
            true
          case Some(response) if !response.isCreated =>
            logger.info("Document has been indexed (update), retrieving headers= " + getHeaderString(response))
            true
          case None =>
            logger.error("Elastic execute result IndexResponse is null, I treat it as an error but can do nothing")
            false
        }
      case _ =>
        logger.error("Refuse to index document that is not a JsObject")
        throw NotAJsObjectException(doc)
    }
  }


  /**
   * Executes a remove operation via the underlying ES client.
   * @param indexName ES index name
   * @param doctype ES document type
   * @param id id of the document that should be removed
   * @return
   */
  private def executeRemove(indexName: String, doctype: String, id: String) =
    client.execute {
      logger.debug(s"Remove document from index=$indexName type=$doctype id=$id")
      delete
        .id(id)
        .from(indexName -> doctype)
    }

  def remove(indexName: String, doctype: String, id: String)
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

case class NotAJsObjectException[A : Writes](doc: A) extends Exception(s"Document ${doc.toString} is not a JSON object")
