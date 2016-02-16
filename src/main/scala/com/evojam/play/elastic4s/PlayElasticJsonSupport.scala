package com.evojam.play.elastic4s

import scala.language.implicitConversions

import play.api.libs.json.{Json, Reads}

import com.sksamuel.elastic4s.{RichSearchHit, HitAs, RichGetResponse}

import com.evojam.play.elastic4s.json.GetResponseWithJson

trait PlayElasticJsonSupport {
  implicit def playElasticJsonGetResponsePimp(r: RichGetResponse): GetResponseWithJson =
    new GetResponseWithJson(r.original)

  implicit def JsonReadsToHitAs[A: Reads]: HitAs[A] = new HitAs[A] {
    override def as(hit: RichSearchHit): A = Json.parse(hit.source).as[A]
  }

}
