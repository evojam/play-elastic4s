package com.evojam.play.elastic4s.client

import scala.concurrent.Future

import play.api.libs.json.Json

import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.search.{SearchHits, SearchHit}
import org.specs2.concurrent.ExecutionEnv
import org.specs2.mock._
import org.specs2.mutable.Specification

import com.sksamuel.elastic4s
import com.sksamuel.elastic4s.{Executable, SearchDefinition}
import com.sksamuel.elastic4s.ElasticDsl._

class ElasticClientImplSpec(implicit ee: ExecutionEnv) extends Specification with Mockito {

  object mocked {
    val es = mock[elastic4s.ElasticClient]
    val document = Json.obj(
      "_id" -> "myid",
      "title" -> "Memoirs of Jim",
      "author" -> Json.obj(
        "name" -> "Jim Timmings",
        "age" -> 34
      ),
      "tags" -> Json.arr("action", "diary")
    )
    val hitBytes = Json.stringify(document).toCharArray.map(_.toByte)
    val searchHit: SearchHit = mock[SearchHit].source returns hitBytes
    val searchHits: SearchHits = mock[SearchHits].getHits returns Array(searchHit)
    val response: SearchResponse = mock[SearchResponse].getHits returns searchHits
  }

  object expected {
    val author = Person("Jim Timmings", 34)
    val book = Book("myid", "Memoirs of Jim", author, Set("action", "diary"))
  }

  val client = new ElasticSearchClientImpl(mocked.es)

  "ElasticSearchClient" should {
    "parse search responses" in {
      val anySearchDef = any[SearchDefinition]
      val anySearchDefExecutable = any[Executable[SearchDefinition, _, SearchResponse]]
      mocked.es.execute(anySearchDef)(anySearchDefExecutable) returns Future.successful(mocked.response)
      client.search(search in "booksIdx" / "books" query "something").collect[Book] should beEqualTo(List(expected.book)).await
    }
  }

  implicit val personFmt = Json.format[Person]
  implicit val bookFmt = Json.format[Book]
}

case class Person(name: String, age: Int)
case class Book(_id: String, title: String, author: Person, tags: Set[String])


