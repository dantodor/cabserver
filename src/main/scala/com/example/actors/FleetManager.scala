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

class FleetManager(howManyCars:Int, initialPoint: Point) extends Actor {

  implicit val timeout = akka.util.Timeout(1 second)
  implicit val ec = context.dispatcher
  val log = Logging(context.system, this.getClass)

  //build the fleet of cabs
  val fleet = (1 to howManyCars) map { i => context.actorOf(Props(new Cab(initialPoint)), name = s"${i}")} toList

  val bCabs = List.empty[ActorRef]

  override def preStart(): Unit = self ! Start //upon startup, switch to working state

  def receive = {
    case Start => context become working(fleet, bCabs)
    case m => log.debug(s"Unknown message received : ${m.toString}")
  }

  // tail recursive Receive function
  // in order to avoid mutable state, we always recurse and modify the internal state
  // i.e. the two lists, free cabs and busy cabs
  def working(freeCabs:List[ActorRef], busyCabs:List[ActorRef]): Receive = {

    case crq:CarRequest => {
      log.debug(s"Received car request: ${crq}")
      // since we will be working with futures, we need a reference to the requester
      val who = sender
      // we ask each car in the free list to tell the distance from this request
      val responses = Future.sequence(freeCabs map { car => (car ? Checkpoint(crq.from)).mapTo[Distance]})

      responses onComplete {
        case Success(s) => {
          // all the futures have completed succasfully
          // we need to find the most appropriate cab for this request
          // so, we do a multi-criterial sort on received responses ( first on distance, then on car id )
          s.sortBy(r => (r.dist, r.car.path.name.toInt)).headOption match {
            case Some(dist) => {
              //the returned list was not empty, we have a winner
              //ask him to take the booking, pipe the acceptance message back to the caller
              (dist.car ? Book(crq)) pipeTo who
              //go back to working mode, but modify the free and busy lists to reflect this booking
              context become working(freeCabs.filter(_!=dist.car),dist.car::busyCabs)
            }
            // list was empty, i.e. all cars are booked
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
      // free all cars in the pool
      fleet map (car => car ! Free(initialPoint) )
      // reset to working state with all cars free to be booked
      context become working(fleet,List.empty[ActorRef])
      sender ! GenericAPIResponse("Reset done")
    }
    case Timestep => {
      log.debug("Time step")
      // send a Time step to all the cars in the busy pool
      busyCabs map (car => car ! Timestep )
      sender ! GenericAPIResponse("Timestep done")
    }
    case TripFinished => {
      //message received from a cab upon trip completion
      //move it from the busy pool to the free pool and resume normal operation
      context become working(sender::freeCabs,busyCabs.filter(_!=sender))
    }
    case m => log.debug(s"Unknown message received : ${m.toString}")
  }

}
