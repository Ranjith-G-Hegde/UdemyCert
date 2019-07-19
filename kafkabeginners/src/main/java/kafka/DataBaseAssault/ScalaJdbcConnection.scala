package kafka.DataBaseAssault

import java.sql.{Connection, DriverManager}

import org.slf4j.LoggerFactory

 object ScalaJdbcConnection {
   val driver = "com.mysql.cj.jdbc.Driver"
   val url = "jdbc:mysql://localhost/db"
   val username = "root"
   val password = "root"
   val logger = LoggerFactory.getLogger("DataBase")
  def selectDB() {
    var connection: Connection = null
    try {
      Class.forName(driver)
      connection = DriverManager.getConnection(url, username, password)

      val statement = connection.createStatement()
      val resultSet = statement.executeQuery("SELECT tname, tinfo FROM tweet")
      while (resultSet.next()) {
        val name = resultSet.getString("tname")
        val weapon = resultSet.getString("tinfo")
        logger.info(s"name, tweet = $name , $weapon")
      }
    } catch {
      case e => e.printStackTrace
    }
    connection.close()
  }

  def insertDB(name: String, weapon: String) {
    var connection: Connection = null
    try {
      Class.forName(driver)
      connection = DriverManager.getConnection(url, username, password)

      val statement = connection.createStatement()
      val insert = "INSERT INTO tweet (tname, tinfo)" +
        "VALUES (\"" + name + "\",\"" + weapon + "\")"
      statement.executeUpdate(insert)
      // statement.updateQuery(insert)
      logger.info("Inserted = " + name + ", " + weapon)
    } catch {
      case e => e.printStackTrace
    }
    connection.close()
  }


  def deleteDB(name: String) {
    var connection: Connection = null

    try {
      Class.forName(driver)
      connection = DriverManager.getConnection(url, username, password)

      val statement = connection.createStatement()
      val delete = s"DELETE FROM tweet WHERE tname = " +"\"name\""
      statement.executeUpdate(delete)
      logger.info("Deleted = " + name + "\n")
    } catch {
      case e => e.printStackTrace
    }
    finally {
      connection.close()
    }

  }

}









