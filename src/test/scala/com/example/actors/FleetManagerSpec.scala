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

  val manager = system.actorOf(Props(new FleetManager(3,Point(0,0))),"manager")

  import com.example.config.ServerSettings.cabs

  "A fleet manager actor " must {
    "Return the appropriate cab to a request" in {
      manager ! CarRequest(Point(2,2),Point(6,10))
      expectMsg(BookResponse(1,16))
    }

    "Be able to fulfill multiple requests" in {
      manager ! Reset
      expectMsg(GenericAPIResponse("Reset done"))
      manager ! CarRequest(Point(2,2),Point(10,10))
      expectMsg(BookResponse(1,20))
      manager ! CarRequest(Point(4,4),Point(6,10))
      expectMsg(BookResponse(2,16))

    }

    "When all cars booked, it should respond with empty response" in {
      manager ! Reset
      expectMsg(GenericAPIResponse("Reset done"))
      (1 to cabs) foreach { i =>
        manager ! CarRequest(Point(2, 2), Point(10, 10))
        expectMsg(BookResponse(i, 20))
      }
      manager ! CarRequest(Point(4,4),Point(10,10))
      expectMsg(BookResponse(0,0))

    }

    "After completing a drive, closest car should be choosen" in {

      manager ! Reset
      expectMsg(GenericAPIResponse("Reset done"))
      manager ! CarRequest(Point(2,2),Point(30,100))
      expectMsg(BookResponse(1,130))
      (0 to 130) foreach {i => {
          manager ! Timestep
          expectMsg(GenericAPIResponse("Timestep done"))
        }
      }
      manager ! CarRequest(Point(-1,-1),Point(-10,-100))
      expectMsg(BookResponse(2,110))

    }

  }
}