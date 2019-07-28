package com.example.actors

import akka.actor.{Actor, ActorRef, Props}
import akka.event.Logging
import akka.pattern.{ask, pipe}
import com.example.model.Models.{BookResponse, CarRequest, GenericAPIResponse, Point}
import com.example.model.Protocol._

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success}

class FleetManager2(howManyCars:Int) extends Actor {

  implicit val timeout = akka.util.Timeout(1 second)
  implicit val ec = context.dispatcher
  val log = Logging(context.system, this.getClass)


  val fleet = (1 to howManyCars) map { i => context.actorOf(Props(new Cab(Point(1,1))), name = s"${i}")} toList

  val bCabs = List.empty[ActorRef]

  override def preStart(): Unit = self ! Start

  def receive = {
    case Start => context become working(fleet,bCabs)
    case mess => log.debug(mess.toString())
  }

  def working(freeCabs:List[ActorRef], busyCabs:List[ActorRef]): Receive = {
    case crq:CarRequest => {
      log.debug(s"Received car request: ${crq}")
      // since we will be working with futures, we need a reference to the requester
      val who = sender
      // we ask each car in the free list to tell the distance from this request
      val responses = Future.sequence(freeCabs map { car => (car ? Checkpoint(crq.from)).mapTo[Distance]})

      responses onComplete {
        case Success(s) => {
          s.sortBy(r => (r.dist, r.car.path.name.toInt)).headOption match {
            case Some(dist) => {
              (dist.car ? Book(crq)) pipeTo who
              context become working(freeCabs.filter(_!=dist.car),dist.car::busyCabs)
            }
            case None => who ! BookResponse(0,0)
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
      context become working(fleet,List.empty[ActorRef])
      sender ! GenericAPIResponse("Reset done")
    }
    case Timestep => {
      //log.debug("Time step")
      busyCabs map (car => car ! Timestep )
      sender ! GenericAPIResponse("Timestep done")
    }
    case TripFinished => context become working(sender::freeCabs,busyCabs.filter(_!=sender))
    case m => log.debug(s"Unknown message received : ${m.toString}")
  }

}
