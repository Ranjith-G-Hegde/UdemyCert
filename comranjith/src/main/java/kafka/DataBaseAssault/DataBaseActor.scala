package kafka.DataBaseAssault

import java.util.logging.{Level, Logger}

import akka.actor.{Actor, ActorRef, Props}
import org.slf4j.LoggerFactory




object DataBaseActor {

  object DBMaster {
    case class Initialize(nChildren: Int)
    case class DataBaseTask(id: Int, name: String=null, weapon: String=null)
    case class DataBaseReply(id: Int)
    case class DBInsert(name: String, weapon: String)
    case class DBDelete(name: String)
  }

  class DBMaster extends Actor{
    import DBMaster._
    val logger = LoggerFactory.getLogger("DBActorMaster")
    override def receive: Receive = {
      case Initialize(nChildren) =>
        logger.info("[master] initializing...")
        val childrenRefs = for (i <- 1 to nChildren) yield context.actorOf(Props[DBWorker], s"db_$i")
        context.become(withChildren(childrenRefs, 0, 0, Map()))
    }

    def withChildren(childrenRefs: Seq[ActorRef], currentChildIndex: Int, currentTaskId: Int, requestMap: Map[Int, ActorRef]): Receive = {
      case DBInsert(name,weapon) =>
        logger.info(s"[master] I have received: $name and $weapon - I will send it to child $currentChildIndex")
        val originalSender = sender()
        val task = DataBaseTask(currentTaskId, name,weapon)
        val childRef = childrenRefs(currentChildIndex)
        childRef ! task
        val nextChildIndex = (currentChildIndex + 1) % childrenRefs.length
        val newTaskId = currentTaskId + 1
        val newRequestMap = requestMap + (currentTaskId -> originalSender)
        context.become(withChildren(childrenRefs, nextChildIndex, newTaskId, newRequestMap))
      case DBDelete(name) =>
        logger.info(s"[master] I have received: $name - I will send it to child $currentChildIndex")
        val originalSender = sender()
        val task = DataBaseTask(currentTaskId, name)
        val childRef = childrenRefs(currentChildIndex)
        childRef ! task
        val nextChildIndex = (currentChildIndex + 1) % childrenRefs.length
        val newTaskId = currentTaskId + 1
        val newRequestMap = requestMap + (currentTaskId -> originalSender)
        context.become(withChildren(childrenRefs, nextChildIndex, newTaskId, newRequestMap))
      case "select" =>
        logger.info(s"[master] I have received: select - I will send it to child $currentChildIndex")
        val originalSender = sender()
        val task = DataBaseTask(currentTaskId)
        val childRef = childrenRefs(currentChildIndex)
        childRef ! task
        val nextChildIndex = (currentChildIndex + 1) % childrenRefs.length
        val newTaskId = currentTaskId + 1
        val newRequestMap = requestMap + (currentTaskId -> originalSender)
        context.become(withChildren(childrenRefs, nextChildIndex, newTaskId, newRequestMap))
      case DataBaseReply(id) =>
        logger.info(s"[master] Request completed for task id $id ")
       // val originalSender = requestMap(id)
        //originalSender ! count
        context.become(withChildren(childrenRefs, currentChildIndex, currentTaskId, requestMap - id))
    }

  }

  class DBWorker extends Actor{
    import DBMaster._
    //val logger = LoggerFactory.getLogger("DBActorWorker")
    LoggerObject.setup()
    val logger=Logger.getLogger(Logger.GLOBAL_LOGGER_NAME)
    logger.setLevel(Level.INFO)
    override def receive: Receive = {
      case DataBaseTask(id,name,weapon) =>
        if(name!=null && weapon!=null) {
          logger.info(s"${self.path} I have received task $id with $name and $weapon")
          ScalaJdbcConnection.insertDB(name,weapon)
          sender() ! DataBaseReply(id)
        }
        else if(name!=null && weapon == null){
          logger.info(s"${self.path} I have received task $id with $name and $weapon")
          ScalaJdbcConnection.deleteDB(name)
          sender() ! DataBaseReply(id)
        }
        else if(name == null && weapon == null)
        {
          logger.info(s"${self.path} I have received task $id with $name and $weapon")
          ScalaJdbcConnection.selectDB()
          sender() ! DataBaseReply(id)
        }else{
          logger.info("error")
        }
    }
  }

}








