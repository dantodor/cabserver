
package com.test.routes

import akka.http.scaladsl.coding.Gzip
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import ch.megard.akka.http.cors.CorsDirectives._

trait BaseRoute {
  def api(dsl: Route, prefix: Boolean=false, version: String = ""): Route =
    cors() {
      pathPrefix(version)(encodeResponseWith(Gzip)(dsl))
    }
}
