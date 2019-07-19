package kafka.DataBaseAssault

import java.util.logging.{FileHandler, Logger, SimpleFormatter,Level}
;


object LoggerObject {

  def setup()={
    val logger: Logger=Logger.getLogger(Logger.GLOBAL_LOGGER_NAME)
    val fileText=new FileHandler("Logging.txt")
    val formatterTxt=new SimpleFormatter()
    fileText.setFormatter(formatterTxt)
    logger.setLevel(Level.INFO)
    logger.addHandler(fileText)
  }

}
