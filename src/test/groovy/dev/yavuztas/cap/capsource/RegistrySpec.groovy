package dev.yavuztas.cap.capsource

import dev.yavuztas.cap.capsource.feed.FeedData
import io.vertx.core.Vertx
import io.vertx.core.net.NetSocket
import spock.lang.Specification
import spock.util.concurrent.AsyncConditions

import java.nio.charset.StandardCharsets
import java.util.function.BiConsumer

class RegistrySpec extends Specification {

  Vertx vertx
  Registry registry

  def setup() {
    vertx = Vertx.vertx()
    registry = new Registry(vertx, Collections.emptyList())

    def ac = new AsyncConditions(1)
    def future = registry.init()
    future.onComplete(r -> {
      ac.evaluate { assert r.succeeded() }
    })
    ac.await()
  }

  def cleanup() {
    registry.destroy()
  }

  NetSocket createClient(BiConsumer<NetSocket, String> onMessage) {
    def ac = new AsyncConditions(1)
    NetSocket socket = null
    def client = vertx.createNetClient()
    client.connect(7000, "localhost", r -> {
      if (r.succeeded()) {
        socket = r.result()
        ac.evaluate { assert true }
      } else {
        println("Failed to connect: ${r.cause().getMessage()}")
      }
    })
    ac.await()

    socket.handler(buffer -> {
      // consume buffer
      int pos = 0
      while (pos < buffer.length()) {
        def messageSize = buffer.getUnsignedShort(pos)
        pos += 2
        def message = buffer.getString(pos, pos + messageSize, StandardCharsets.US_ASCII.name())
        pos += messageSize
        // delegate to consumer
        onMessage.accept(socket, message)
      }
    })

    return socket
  }

  def 'test connect clients'() {
    given:
    def ac = new AsyncConditions(20)
    def clients = []
    for (i in 0..<10) {
      clients.add(createClient((s,m) -> {
        println("client#${s.localAddress()} got message: ${m}")
        ac.evaluate { assert true }
      }))
    }

    when:
    for (i in 0..<2) {
      registry.publish(new FeedData("Booo!").asReadOnly())
    }

    then:
    ac.await()

    cleanup:
    clients.each { (it as NetSocket).close() }
  }

  def 'test publish slow client'() {
    // TODO
  }

}
