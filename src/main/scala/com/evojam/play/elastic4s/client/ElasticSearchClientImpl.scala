package com.evojam.play.elastic4s.client

import scala.concurrent.{ExecutionContext, Future}
import scala.language.implicitConversions

import play.api.Logger
import play.api.libs.json._

import org.elasticsearch.action.ActionResponse
import org.elasticsearch.action.bulk.BulkResponse
import org.elasticsearch.action.index.IndexResponse

import com.google.inject.Inject
import com.sksamuel.elastic4s.{IndexType, IndexDefinition, ElasticClient, SearchDefinition}
import com.sksamuel.elastic4s.ElasticDsl.{index => elastic4sindex, _}
import com.sksamuel.elastic4s.source.{DocumentSource, JsonDocumentSource}

import com.evojam.play.elastic4s.core.search.PreparedSearch

class ElasticSearchClientImpl (val client: ElasticClient) extends ElasticSearchClient {

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

  private def doc2source[T: Writes](document: T): DocumentSource = Json.toJson(document) match {
    case obj: JsObject => JsonDocumentSource(Json.stringify(obj))
    case other => throw NotAJsObjectException(other)

  }

  private implicit class RichIndexDefinition(underlying: IndexDefinition) {

    def optionalId(maybeId: Option[String]) = maybeId
      .map(id => underlying.id(id))
      .getOrElse(underlying)
  }

  /**
   * Executes an index operation via the underlying ES client.
   *
   * If a document with the same id already exists, it will be overwritten.
   *
   * @param indexType type of ES document to be indexed
   * @param id id of the document
   * @param in the document to be indexed
   * @param exc the concurrent execution context
   * @return
   */
  private def executeIndex(indexType: IndexType, id: Option[String], in: DocumentSource)
      (implicit exc: ExecutionContext): Future[Option[IndexResponse]] =
    client.execute {
      logger.debug(s"Index with indexType=$indexType doc=${in.json}")
      elastic4sindex
        .into(indexType)
        .doc(in)
        .optionalId(id)
    } map Option.apply


  def search(searchDef: SearchDefinition): PreparedSearch =
    PreparedSearch(searchDef, client)


  def index[T: Writes](indexType: IndexType, id: Option[String], doc: T)
      (implicit exc: ExecutionContext): Future[Boolean] = {
    require(indexType != null, "indexType cannot be null")
    require(id != null, "id cannot be null")
    require(doc != null, "doc cannot be null")

    executeIndex(indexType, id, doc2source(doc)).map {
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
  }

  def index[T: Writes](indexType: IndexType, id: String, doc: T)
      (implicit exc: ExecutionContext): Future[Boolean] = {
    require(id != null, "id cannot be null")
    index(indexType, Some(id), doc)
  }

  def index[T: Writes](indexType: IndexType, doc: T)(implicit exc: ExecutionContext) =
    index(indexType, None, doc)

  /**
   * Executes a remove operation via the underlying ES client.
   * @param indexType ES index name and document type
   * @param id id of the document that should be removed
   * @return
   */
  private def executeRemove(indexType: IndexType, id: String) =
    client.execute {
      logger.debug(s"Remove document with indexType=$indexType id=$id")
      delete
        .id(id)
        .from(indexType)
    }

  def remove(indexType: IndexType, id: String)
      (implicit exc: ExecutionContext): Future[Boolean] = {
    require(indexType != null, "indexType cannot be null")
    require(id != null, "id cannot be null")

    executeRemove(indexType, id).map {
      case ok if ok.isFound => true
      case _ =>
        logger.warn(s"Document with indexType=$indexType id=$id not found")
        false
    }
  }

  private def executeBulkInsert(indexType: IndexType, documents: Iterable[DocumentSource]) =
    client.execute {
      bulk (
        documents.map(source => elastic4sindex.into(indexType).doc(source))
      )
    }

  def bulkInsert[T: Writes](indexType: IndexType, documents: Iterable[T]): Future[BulkResponse] = {
    require(indexType != null, "index name cannot be null")
    require(documents != null, "documents cannot be null")
    executeBulkInsert(indexType, documents.map(doc2source(_)))
  }
}

case class NotAJsObjectException[A : Writes](doc: A) extends Exception(s"Document ${doc.toString} is not a JSON object")
