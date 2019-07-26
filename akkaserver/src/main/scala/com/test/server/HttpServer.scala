package com.test.server

import akka.http.scaladsl.Http
import com.test.routes.ServerRoutes
import com.test.config.ServerSettings._

object HttpServer extends App {

  Http(actorSystem).bindAndHandle(ServerRoutes.availableRoutes, httpInterface, httpPort)
  log.info(s"\nAkka HTTP Server - Version ${actorSystem.settings.ConfigVersion} - running at http://$httpInterface:$httpPort/")

}
