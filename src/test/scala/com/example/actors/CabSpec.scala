package com.example.actors

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestActors, TestKit}
import com.example.model.Models.{BookResponse, CarRequest, Point}
import com.example.model.Protocol.{Book, Booked, Checkpoint, Distance, Free, Timestep}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class CabSpec extends TestKit(ActorSystem("MySpec"))
  with ImplicitSender
  with WordSpecLike
  with Matchers
  with BeforeAndAfterAll {

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }

  val cab = system.actorOf(Props(new Cab(Point(1,1))),"1")

  "A cab actor " must {
    "Calculate the appropriate distance" in {
      cab ! Checkpoint(Point(2,2))
      expectMsg(Distance(cab,2))
    }
    "Accept a booking and answer appropriately" in {
      cab ! Book(CarRequest(Point(2,3),Point(10,10)))
      expectMsg(BookResponse(1,18))
    }

    "Accept a booking and drive around" in {
      //incidentally, here we also verify the reset logic
      cab ! Free(Point(1,1))
      cab ! Book(CarRequest(Point(2,2),Point(3,3)))
      expectMsg(BookResponse(1,4))
      (1 to 4). foreach(i=>cab ! Timestep)
      //the above should complete the trip, so we should be able to book the car again
      cab ! Book(CarRequest(Point(5,7),Point(10,4)))
      expectMsg(BookResponse(1,14))
    }

    "Respond with Booked when requested a new booking while driving" in {
      cab ! Free(Point(1,1))
      cab ! Book(CarRequest(Point(2,2),Point(3,3)))
      expectMsg(BookResponse(1,4))
      (1 to 3). foreach(i=>cab ! Timestep)
      //the above should not complete the trip
      cab ! Book(CarRequest(Point(5,7),Point(10,4)))
      expectMsg(Booked)
    }


  }
}