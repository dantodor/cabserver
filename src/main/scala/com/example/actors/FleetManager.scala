package com.example.actors

import akka.actor.{Actor, Props}
import akka.event.Logging
import akka.pattern.ask
import com.example.model.Models.{CarRequest, Point}
import com.example.model.Protocol.{Book, Checkpoint, Distance, Free, Reset, Timestep}
import scala.language.postfixOps
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success}

class FleetManager(howManyCars:Int) extends Actor {

  implicit val timeout = akka.util.Timeout(1 second)
  implicit val ec = context.dispatcher
  val log = Logging(context.system, this.getClass)

  val fleet = (0 to howManyCars) map { i => context.actorOf(Props(new Cab(Point(1,1))), name = s"Cab ${i}")}

  def receive = {
    case crq:CarRequest => {
      val who = sender // since we will be working with futures, we need a reference to the requester
      // we ask each car to tell the distance from this request
      val s = Future.sequence(fleet map { car => (car ? Checkpoint(crq.from)).mapTo[Any]}) map (_.collect{case Success(x)=>x})

      s onComplete {
        case Success(s) => {
          // all succesfull futures are collected
          // possible answers are the distance from the requester or car booked
          //so we filter only the cars that answered with a distance
          val v = s collect {
            case d:Distance => d
          }
          // check if we have any car
          if(v.length>0) {
            val car = v.minBy(dist=> dist.i).car
            car ! Book(crq)
            who ! s"success, we booked you ${car.path.name}"
          } else {
            who ! "All cabs are booked, please retry"
          }
        }

        case Failure(exception) => { // somehow our Future has failed, inform requester and log error
          log.error(exception.getMessage)
          who ! s"Error while trying to book: ${exception.getMessage}"
        }
      }
    }

    case Reset => fleet map (car => car ! Free(Point(1,1)) )
    case Timestep => fleet map (car => car ! Timestep )
    case m => log.debug(s"Unknown message received : ${m.toString}")
  }

}
