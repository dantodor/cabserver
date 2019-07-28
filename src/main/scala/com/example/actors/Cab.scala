package com.example.actors

import akka.actor.Actor
import akka.event.Logging
import com.example.model.Models.{BookResponse, CarRequest, Point}
import com.example.model.Protocol.{Book, Booked, Checkpoint, Distance, Free, Timestep, TripFinished}


class Cab(initialPosition:Point) extends Actor {

  import context._

  val log = Logging(context.system, this.getClass)
  //val position = initialPosition

  override def preStart(): Unit = {
    self ! Free(initialPosition)
  }

  def receive = {
    case Free(pos) => {
      become(waiting(pos))
    }
    case m => log.debug(s"receive -> Unknown message received: ${m.toString}")
  }

  def waiting(pos: Point): Receive = {

    case Book(req) => { // we have a booking order, so we start driving
      //the distance we have to drive is from current position to pickup point added to the trip distance
      val dist = manhattan(pos,req.from) + manhattan(req.from, req.to)
      //signal the fleet manager that we accepted the trip
      sender ! BookResponse(self.path.name.toInt,dist)
      // switch to driving mode
      become(driving(req, dist))
    }

    case check:Checkpoint => sender ! Distance(self,manhattan(pos,check.where))//send back the calculated distance

    case Free(pos) => become(waiting(pos)) //reset request

    case Timestep => // do nothing, we are not moving

    case m => log.debug(s"waiting -> Unknown message received: ${m.toString}")
  }

  def driving(req:CarRequest, dist:Long): Receive = {

    case Timestep => {
      if (dist>1){ // one step forward towards destination
        become(driving(req,dist-1))
      } else {
        // reached destination
        // signal the FleetManager that we are free and switch to waiting state
        parent ! TripFinished
        become(waiting(req.to))
      }
    }

    //we are still driving, but we received a booking request
    // this might be caused by a nasty race condition
    // this message will tell the sender that something is wrong
    case b:Book => sender ! Booked

    // we got a distance calculation request, but we are driving. Send the max allowed value, to make sure
    // we don't get the booking. In theory, we should never reach this branch, this is safety net only
    case Checkpoint(p) => sender ! Distance(self, Long.MaxValue)

    case Free(pos) => become(waiting(pos)) // reset request

    case m => log.debug(s"driving -> Unknown message received : ${m.toString}")
  }


  def manhattan (start:Point,end:Point) = {
    Math.abs(start.x-end.x)+Math.abs(start.y-end.y)
  }

}
