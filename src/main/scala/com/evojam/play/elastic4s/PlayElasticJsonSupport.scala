package com.evojam.play.elastic4s

import scala.language.implicitConversions

import play.api.libs.json.{Writes, Json, Reads}

import com.sksamuel.elastic4s.source.Indexable
import com.sksamuel.elastic4s.{RichSearchHit, HitAs, RichGetResponse}

import com.evojam.play.elastic4s.json.GetResponseWithJson

/**
  * Provides interoperability with Play JSON formatters.
  */
trait PlayElasticJsonSupport {
  implicit def playElasticJsonGetResponsePimp(r: RichGetResponse): GetResponseWithJson =
    new GetResponseWithJson(r.original)

  implicit def jsonReadsToHitAs[A: Reads]: HitAs[A] = new HitAs[A] {
    override def as(hit: RichSearchHit): A = Json.parse(hit.source).as[A]
  }

  implicit def jsonWritesToIndexable[A: Writes]: Indexable[A] = new Indexable[A] {
    override def json(t: A): String = Json.toJson(t).toString()
  }

}
