package com.evojam.play.elastic4s.client

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.Future.successful
import scala.language.implicitConversions

import play.api.Logger
import play.api.libs.json._

import org.elasticsearch.action.ActionResponse
import org.elasticsearch.action.index.IndexResponse

import com.google.inject.Inject
import com.sksamuel.elastic4s.{ElasticClient, SearchDefinition}
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.source.{DocumentSource, JsonDocumentSource}

import com.evojam.play.elastic4s.core.search.PreparedSearch

class ElasticSearchClientImpl @Inject() (val client: ElasticClient) extends ElasticSearchClient {

  private[this] val logger = Logger(getClass)

  private def getHeaders[R <: ActionResponse](response: R) =
    Option(response.getHeaders)
      .map(_.toString)
      .getOrElse("<null>")

  private def executeUpsert(indexName: String, doctype: String, id: String, in: DocumentSource)
      (implicit exc: ExecutionContext): Future[Option[IndexResponse]] =
    client.execute {
      logger.debug(s"Index into index=$indexName type=$doctype doc=${in.json}")
      index.into(indexName -> doctype)
        .doc(in)
        .id(id)
    } map Option.apply

  def search(searchDef: SearchDefinition): PreparedSearch =
    PreparedSearch(searchDef, client)


  def upsert[T: Writes](indexName: String, doctype: String, id: String, doc: T)
      (implicit exc: ExecutionContext): Future[Boolean] = {
    require(indexName != null, "indexName cannot be null")
    require(doctype != null, "doctype cannot be null")
    require(id != null, "id cannot be null")
    require(doc != null, "doc cannot be null")

    Json.toJson(doc) match {
      case json: JsObject =>
        executeUpsert(indexName, doctype, id, JsonDocumentSource(Json.stringify(json))).map {
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


  private def executeRemove(indexName: String, doctype: String, id: String)(implicit exc: ExecutionContext) =
    client.execute {
      logger.debug(s"Remove document from index=$indexName type=$doctype id=$id")
      delete
        .id(id)
        .from(indexName -> doctype)
    }

  override def remove(indexName: String, doctype: String, id: String)
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
