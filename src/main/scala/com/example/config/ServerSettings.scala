package com.example.config

import akka.actor.{ActorSystem, Props}
import akka.event.{LogSource, Logging}
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.example.actors.FleetManager
import com.example.model.Models.Point
import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.duration._
import scala.concurrent.ExecutionContextExecutor
import scala.language.postfixOps

trait ServerSettings {

  //read configuration and initialize values
  private val config: Config = ConfigFactory.load()
  val httpConfig = config.getConfig("http")
  val httpInterface: String = httpConfig.getString("interface")
  val httpPort: Int = httpConfig.getInt("port")
  val fleetConfig = config.getConfig("fleet")
  val cabs = fleetConfig.getInt("cabs")
  val initialX = fleetConfig.getInt("posx")
  val initialY = fleetConfig.getInt("posy")


  //initialize and set up the actor system
  implicit val actorSystem: ActorSystem = ActorSystem("cabserver")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = actorSystem.dispatcher
  implicit val timeout = Timeout(3 seconds)

  // initialize logging system
  private implicit val logSource: LogSource[ServerSettings] = (t: ServerSettings) ⇒ t.getClass.getSimpleName
  private def logger(implicit logSource: LogSource[_ <: ServerSettings]) = Logging(actorSystem, this.getClass)
  implicit val log = logger

  //start the fleet manager actor
  val fleetManager = actorSystem.actorOf(Props(new FleetManager(cabs,Point(initialX,initialY))),"FleetManager")
}

object ServerSettings extends ServerSettings
