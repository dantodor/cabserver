package com.test.actors

import akka.actor.Actor
import akka.event.Logging
import com.test.model.Models.{CarRequest, Point}
import com.test.model.Protocol.{Book, Booked, Checkpoint, Distance, Free, Timestep}


class Cab(initialPosition:Point) extends Actor {

  val log = Logging(context.system, this.getClass)
  val position = initialPosition

  override def preStart(): Unit = {
    self ! Free(initialPosition)
  }

  def receive = {
    case Free(pos) => context become waiting(pos)
    case m => log.debug(s"Unknown message received in waiting: ${m.toString}")
  }

  def waiting(pos: Point): Receive = {
    case Book(req) => { // we have a booking order, so we start driving
      //the distance we have to drive is from current position to pickup point added to the trip distance
      val dist = manhattan(pos,req.from) + manhattan(req.from, req.to)
      // start driving
      context become driving(req, dist)
    }
    case Checkpoint(p) => sender ! Distance(self,manhattan(pos,p))
    case Free(pos) => context become waiting(pos) //reset request
    case Timestep => {} // do nothing, we are not moving
    case m => log.debug(s"Unknown message received in waiting: ${m.toString}")
  }

  def driving(req:CarRequest, dist:Long): Receive = {
    case Timestep => if (dist>1){ // one step forward towards destination
                        context become driving(req,dist-1)
                      } else {
                        context become waiting(req.to)
                      }
    case b:Book => sender ! Booked //we are still driving, so tell the requester
    case Checkpoint(p) => sender ! Booked
    case Free(pos) => context become waiting(pos) // reset request
    case m => log.debug(s"Unknown message received in working: ${m.toString}")
  }


  def manhattan (start:Point,end:Point) = {
    Math.abs(start.x-end.x)+Math.abs(start.y-end.y)
  }

}
