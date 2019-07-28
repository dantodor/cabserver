package com.example.actors

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import com.example.model.Models.{BookResponse, CarRequest, GenericAPIResponse, Point}
import com.example.model.Protocol._
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class FleetManagerSpec extends TestKit(ActorSystem("MySpec"))
  with ImplicitSender
  with WordSpecLike
  with Matchers
  with BeforeAndAfterAll {

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }

  val manager = system.actorOf(Props(new FleetManager(2)),"manager")

  "A fleet manager actor " must {
    "Return the appropriate cab to a request" in {
      manager ! CarRequest(Point(2,2),Point(6,10))
      expectMsg(BookResponse(1,14))
    }

    "Be able to fulfill multiple requests" in {
      manager ! Reset
      expectMsg(GenericAPIResponse("Done"))
      manager ! CarRequest(Point(2,2),Point(10,10))
      expectMsg(BookResponse(1,18))
      manager ! CarRequest(Point(4,4),Point(6,10))
      expectMsg(BookResponse(2,14))

    }

    "When all cars booked, it should respond with empty response" in {
      manager ! Reset
      expectMsg(GenericAPIResponse("Done"))
      manager ! CarRequest(Point(2,2),Point(10,10))
      expectMsg(BookResponse(1,18))
      manager ! CarRequest(Point(4,4),Point(10,10))
      expectMsg(BookResponse(2,18))
      manager ! CarRequest(Point(4,4),Point(10,10))
      expectMsg(BookResponse(0,0))
    }

    "After completing a drive, closest car should be choosen" in {
      manager ! Reset
      expectMsg(GenericAPIResponse("Done"))
      manager ! CarRequest(Point(2,2),Point(3,3))
      expectMsg(BookResponse(1,4))
      (0 to 4) foreach {i => {
        manager ! Timestep
        expectMsg(GenericAPIResponse("Done"))
      }
      }
      manager ! CarRequest(Point(4,4),Point(10,10))
      expectMsg(BookResponse(1,14))

      manager ! Reset
      expectMsg(GenericAPIResponse("Done"))
      manager ! CarRequest(Point(2,2),Point(100,30))
      expectMsg(BookResponse(1,128))
      (0 to 128) foreach {i => {
          manager ! Timestep
          expectMsg(GenericAPIResponse("Done"))
        }
      }
      manager ! CarRequest(Point(-1,-1),Point(-10,-100))
      expectMsg(BookResponse(2,112))

    }

  }
}