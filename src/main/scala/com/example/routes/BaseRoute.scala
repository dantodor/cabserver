package com.example.routes

import akka.http.scaladsl.server._
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._

trait BaseRoute {
  def api(dsl: Route): Route =
    cors() {
      (dsl)
    }
}
