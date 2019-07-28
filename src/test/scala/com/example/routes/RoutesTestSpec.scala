package com.example.routes

import org.scalatest.{Matchers, WordSpec}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.example.model.Models.{BookResponse, GenericAPIResponse, Point, WebCarRequest}
import de.heikoseeberger.akkahttpcirce.CirceSupport._
import io.circe.generic.auto._

class RoutesTestSpec extends WordSpec with Matchers with ScalatestRouteTest {
  import ServerRoutes._


  "The service" should {

    "execute a timestep in api/tick" in {

      Post("/api/tick") ~> availableRoutes ~> check {
        status shouldEqual StatusCodes.OK
        entityAs[GenericAPIResponse] shouldEqual GenericAPIResponse("Done")
      }
    }

    "execute a reset in api/reset" in {

      Put("/api/reset") ~> availableRoutes ~> check {
        status shouldEqual StatusCodes.OK
        entityAs[GenericAPIResponse] shouldEqual GenericAPIResponse("Done")
      }
    }

    "return a valid booking" in {

      Post("/api/book", WebCarRequest(Point(10, 10), Point(50, 50))) ~> availableRoutes ~> check {
        status shouldEqual StatusCodes.OK
        entityAs[BookResponse] shouldEqual BookResponse(1, 98)

      }
    }


    "return an empty booking when no cars left" in {
        Put("/api/reset") ~> availableRoutes ~> check {
          status shouldEqual StatusCodes.OK
          entityAs[GenericAPIResponse] shouldEqual GenericAPIResponse("Done")
        }
        Post("/api/book",WebCarRequest(Point(10,10),Point(50,50))) ~> availableRoutes ~> check {
          status shouldEqual StatusCodes.OK
          entityAs[BookResponse] shouldEqual BookResponse(1,98)
        }
        Post("/api/book",WebCarRequest(Point(10,10),Point(50,50))) ~> availableRoutes ~> check {
          status shouldEqual StatusCodes.OK
          entityAs[BookResponse] shouldEqual BookResponse(2,98)
        }
        Post("/api/book",WebCarRequest(Point(10,10),Point(50,50))) ~> availableRoutes ~> check {
          status shouldEqual StatusCodes.OK
          entityAs[BookResponse] shouldEqual BookResponse(0,0)
        }

      }


  }

}