/*
 * Copyright 2016 Vitor Vieira
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.test.routes

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import de.heikoseeberger.akkahttpcirce.CirceSupport._
import io.circe.generic.auto._

import scala.concurrent.Future

object ServerRoutes extends BaseRoute with ResponseWrapper {


  protected def templateDirectives: Route =
    pathPrefix("service1") {
      get {
        path("status") {
          extractRequest { req ⇒
            sendResponse(Future(ApiMessage(ApiStatusMessages.currentStatus())))
          }
        }
      } ~
        post {
          path("model") {
            decodeRequest {
              entity(as[Request]) { request =>
                sendResponse(Future(ApiMessage(s"model.vString: ${request.from} - model.vListInt: ${request.to}")))
              }
            }
          }
        }
    }


  protected lazy val api: Route =
    api(dsl = logRequestResult("log-service") {
      this.templateDirectives
    })

  def availableRoutes: Route = api

}
