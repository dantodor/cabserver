package com.example.server

import akka.http.scaladsl.Http
import com.example.routes.ServerRoutes
import com.example.config.ServerSettings._

object CabServer extends App {

  Http(actorSystem).bindAndHandle(ServerRoutes.availableRoutes, httpInterface, httpPort)
  log.info(s"\nCab Server running at http://$httpInterface:$httpPort/")

}
