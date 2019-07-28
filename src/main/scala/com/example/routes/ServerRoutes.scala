package com.example.routes

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.example.model.Models.{BookResponse, GenericAPIResponse, WebCarRequest}
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._
import akka.pattern.ask
import com.example.model.Protocol.{Reset, Timestep}

object ServerRoutes extends BaseRoute {


  import com.example.config.ServerSettings._

  protected def templateDirectives: Route =
    pathPrefix("api") {
      put {
        path("reset") {
            complete((fleetManager ? Reset).mapTo[GenericAPIResponse])
          }

      } ~
        post {
          path("book") {
            decodeRequest {
              entity(as[WebCarRequest]) { request =>
                complete((fleetManager ? request.toCarRequest).mapTo[BookResponse])
              }
            }
          }
        } ~
        post {
          path("tick") {
            complete((fleetManager ? Timestep).mapTo[GenericAPIResponse])
          }
        }
    }


  protected val api: Route =
    api(dsl = logRequestResult("log-service") {
      templateDirectives
    })

  def availableRoutes: Route = api

}
