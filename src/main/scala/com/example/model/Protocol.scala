package com.example.model

import akka.actor.ActorRef
import com.example.model.Models.{CarRequest, Point}

object Protocol {

  case class Distance(car:ActorRef, dist:Long)
  case class Book(req:CarRequest)
  case class Free(position:Point)
  case class Checkpoint(where:Point)
  case object Timestep
  case object Booked
  case object Reset
  case object Start
  case object TripFinished

}
