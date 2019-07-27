package com.test.model

import akka.actor.ActorRef
import com.test.model.Models.{CarRequest, Point}

object Protocol {

  case class Distance(car:ActorRef, i:Long)
  case class Book(req:CarRequest)
  case object WaitForResolution
  case object Timestep
  case object Booked
  case object Reset
  case class Free(position:Point)
  case class Checkpoint(where:Point)

}
