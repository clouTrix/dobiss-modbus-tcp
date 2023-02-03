package testutils

import com.sun.net.httpserver.{HttpExchange, HttpServer}
import org.scalatest.Assertions.fail

import java.net.{InetSocketAddress, ServerSocket}
import scala.util.Using

class MockHttpServer {
  private def anyFreePort = Using.resource(new ServerSocket(0))(_.getLocalPort)
  private val server = HttpServer.create(new InetSocketAddress("localhost", anyFreePort), 0)

  def port: Int = server.getAddress.getPort

  def start(): Unit = server.start()
  def stop(): Unit = server.stop(0)

  private var handlers = Map.empty[String, () => String]

  server.createContext("/", (http: HttpExchange) => handlers.get(http.getRequestURI.getPath)
                                                        .map(_())
                                                        .tapEach(body => http.sendResponseHeaders(200, body.length))
                                                        .map { body =>
                                                            Using.resource(http.getResponseBody) { os =>
                                                              os.write(body.getBytes)
                                                              os.flush()
                                                            }
                                                          }
                                                        .headOption
                                                        .getOrElse { fail() }: Unit)

  def register(path: String)(handler: => String): Unit = handlers += (path -> { () => handler })
}
