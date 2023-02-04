package testutils

import java.io.BufferedInputStream
import java.net.{ServerSocket, Socket}
import scala.util.Using

trait TestSockets {
  def anyFreePort: Int = Using.resource(new ServerSocket(0))(_.getLocalPort)

  def sendTo(port: Int)(data: Array[Byte], expectedResponseLength: Int): Array[Byte] = {
    Using.resource(new Socket("localhost", port)) { conn =>
      val os = conn.getOutputStream
      os.write(data)
      os.flush()


      val is = new BufferedInputStream(conn.getInputStream)
      is.readNBytes(expectedResponseLength)
    }
  }
}
