package kafka.DataBaseAssault

import akka.actor.{ActorSystem, Props}
import kafka.DataBaseAssault.DataBaseActor.DBMaster
import kafka.DataBaseAssault.DataBaseActor.DBMaster.{DBInsert, Initialize}
import org.slf4j.LoggerFactory

object DBApp {
  val system = ActorSystem ("DBSystem")
  val dbActor = system.actorOf (Props[DBMaster] )
  val logger = LoggerFactory.getLogger ("App");

  def initialize(){
    dbActor ! Initialize (5)
  }
  def callToActor(name:String, tweet:String) {
    dbActor ! DBInsert (name, tweet)
  }
  }



