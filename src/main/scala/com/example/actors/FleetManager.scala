package com.example.actors

import akka.actor.{Actor, Props}
import akka.event.Logging
import akka.pattern.ask
import com.example.model.Models.{BookResponse, CarRequest, GenericAPIResponse, Point}
import com.example.model.Protocol.{Book, Checkpoint, Distance, Free, Reset, Timestep}
import akka.pattern.pipe

import scala.language.postfixOps
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success}

class FleetManager(howManyCars:Int) extends Actor {

  implicit val timeout = akka.util.Timeout(1 second)
  implicit val ec = context.dispatcher
  val log = Logging(context.system, this.getClass)

  val fleet = (1 to howManyCars) map { i => context.actorOf(Props(new Cab(Point(1,1))), name = s"${i}")}

  def receive = {
    case crq:CarRequest => {
      log.debug(s"Received car request: ${crq}")
      // since we will be working with futures, we need a reference to the requester
      val who = sender
      // we ask each car to tell the distance from this request
      val responses = Future.sequence(fleet map { car => (car ? Checkpoint(crq.from)).mapTo[Any]})

      responses onComplete {
        case Success(s) => {
          // all succesfully completed futures are collected
          // possible answers are the distance from the requester or car booked
          //so we filter only the cars that answered with a distance
          val v = s collect {
            case d:Distance => d
          }
          // check if we have any car
          if(v.length>0) {
            val minDist = v.minBy(dist=>dist.dist).dist //find the minimal distance
            val cab = v.filter(_.dist==minDist).sortBy(_.car.path.name.toInt).head.car //filter and sort
            (cab ? Book(crq)) pipeTo who
          } else {
            who ! BookResponse(0,0)
          }
        }

        case Failure(exception) => { // somehow our Future has failed, inform requester and log error
          log.error(exception.getMessage)
          who ! BookResponse(0,0)
        }
      }
    }

    case Reset => {
      log.debug("Reseting fleet")
      fleet map (car => car ! Free(Point(1,1)) )
      sender ! GenericAPIResponse("Done")
    }
    case Timestep => {
      //log.debug("Time step")
      fleet map (car => car ! Timestep )
      sender ! GenericAPIResponse("Done")
    }
    case m => log.debug(s"Unknown message received : ${m.toString}")
  }

}
