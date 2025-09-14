package testutils

import com.sun.net.httpserver.{HttpExchange, HttpServer}
import com.typesafe.scalalogging.StrictLogging
import org.scalatest.Assertions.fail

import java.net.InetSocketAddress
import java.util.concurrent.atomic.AtomicBoolean
import scala.util.Using

class MockHttpServer extends TestSockets with StrictLogging {
    private val server = HttpServer.create(new InetSocketAddress("localhost", anyFreePort), 0)
    private val respond401 = new AtomicBoolean(false)
    private var handlers = Map.empty[String, () => String]

    def port: Int = server.getAddress.getPort
    def start(): Unit = server.start()
    def stop(): Unit = server.stop(0)
    def force401(): Unit = respond401.set(true)

    server.createContext("/", (_: HttpExchange) match {
        case http if respond401.get() =>
            respond401.set(false)
            http.sendResponseHeaders(401, -1)

        case http =>
            handlers.get(http.getRequestURI.getPath)
                .map(_())
                .tapEach(body => http.sendResponseHeaders(200, body.length))
                .map { body =>
                    Using.resource(http.getResponseBody) { os => os.write(body.getBytes) ; os.flush() }
                }
                .headOption
                .getOrElse { fail() }: Unit
    })

    def register(path: String)(handler: => String): Unit = handlers += (path -> { () => handler })
}
