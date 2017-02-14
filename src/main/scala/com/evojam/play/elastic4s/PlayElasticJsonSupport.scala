package com.evojam.play.elastic4s

import scala.language.implicitConversions

/**
  * Provides interoperability with Play JSON formatters.
  */
@deprecated("Use import com.sksamuel.elastic4s.playjson._ instead", "0.4.0")
trait PlayElasticJsonSupport {
  import com.sksamuel.elastic4s.playjson._
}
