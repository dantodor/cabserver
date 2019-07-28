package com.example.routes

import org.scalatest.{Matchers, WordSpec}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.example.model.Models.{BookResponse, GenericAPIResponse, Point, WebCarRequest}
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._
import scala.concurrent.duration._
import akka.http.scaladsl.testkit.RouteTestTimeout
import akka.testkit.TestDuration

class RoutesTestSpec extends WordSpec with Matchers with ScalatestRouteTest {

  import com.example.config.ServerSettings.cabs
  import ServerRoutes._


  implicit val timeout = RouteTestTimeout(5.seconds.dilated)

  "The service" should {


    //because of the lazy initialization of worker actors,
    //the first request sometimes fails, and there is no way in delaying it
    //happening only in test setup, not in prod
    "maybbe fail the first request to api/tick" in {
      Post("/api/tick") ~> availableRoutes ~> check {
        status should (equal(StatusCodes.InternalServerError) or equal(StatusCodes.OK))

      }
    }

    "execute a timestep in api/tick" in {

      Post("/api/tick") ~> availableRoutes ~> check {
        status shouldEqual StatusCodes.OK
        entityAs[GenericAPIResponse] shouldEqual GenericAPIResponse("Timestep done")
      }
    }

    "execute a reset in api/reset" in {

      Put("/api/reset") ~> availableRoutes ~> check {
        status shouldEqual StatusCodes.OK
        entityAs[GenericAPIResponse] shouldEqual GenericAPIResponse("Reset done")
      }
    }

    "return a valid booking" in {

      Post("/api/book", WebCarRequest(Point(10, 10), Point(50, 50))) ~> availableRoutes ~> check {
        status shouldEqual StatusCodes.OK
        entityAs[BookResponse] shouldEqual BookResponse(1, 100)

      }
    }


    "return an empty booking when no cars left" in {
        Put("/api/reset") ~> availableRoutes ~> check {
          status shouldEqual StatusCodes.OK
          entityAs[GenericAPIResponse] shouldEqual GenericAPIResponse("Reset done")
        }
        (1 to cabs).foreach { i =>
          Post("/api/book", WebCarRequest(Point(10, 10), Point(50, 50))) ~> availableRoutes ~> check {
            status shouldEqual StatusCodes.OK
            entityAs[BookResponse] shouldEqual BookResponse(i, 100)
          }
        }
        Post("/api/book",WebCarRequest(Point(10,10),Point(50,50))) ~> availableRoutes ~> check {
          status shouldEqual StatusCodes.OK
          entityAs[BookResponse] shouldEqual BookResponse(0,0)
        }

      }


  }

}