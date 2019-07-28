package com.example.actors

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
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

  val cab = system.actorOf(Props(new Cab(Point(0,0))),"1")

  "A cab actor " must {
    "Calculate the appropriate distance" in {
      cab ! Checkpoint(Point(2,2))
      expectMsg(Distance(cab,4))
    }
    "Accept a booking and answer appropriately" in {
      cab ! Book(CarRequest(Point(2,3),Point(10,10)))
      expectMsg(BookResponse(1,20))
    }

    "Be able to accomodate largest request space" in {
        cab ! Free(Point(0,0))
        cab ! Book(CarRequest(Point(Int.MaxValue,Int.MaxValue),Point(Int.MinValue,Int.MinValue)))
        expectMsg(BookResponse(1,12884901884L))
    }

    "Accept a booking and drive around" in {
      //incidentally, here we also verify the reset logic
      cab ! Free(Point(0,0))
      cab ! Book(CarRequest(Point(2,2),Point(3,3)))
      expectMsg(BookResponse(1,6))
      (1 to 6). foreach(i=>cab ! Timestep)
      //the above should complete the trip, so we should be able to book the car again
      cab ! Book(CarRequest(Point(5,7),Point(10,4)))
      expectMsg(BookResponse(1,14))
    }

    "Respond with Booked when requested a new booking while driving" in {
      cab ! Free(Point(0,0))
      cab ! Book(CarRequest(Point(2,2),Point(3,3)))
      expectMsg(BookResponse(1,6))
      (1 to 3). foreach(i=>cab ! Timestep)
      //the above should not complete the trip
      cab ! Checkpoint(Point(0,0))
      expectMsg(Distance(cab,Long.MaxValue))
      cab ! Book(CarRequest(Point(5,7),Point(10,4)))
      expectMsg(Booked)
    }


  }
}