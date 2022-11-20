package dev.yavuztas.cap.capsource.registry

import dev.yavuztas.cap.capsource.config.properties.RegistryProperties
import dev.yavuztas.cap.capsource.feed.FeedConsumer
import dev.yavuztas.cap.capsource.feed.FeedData
import dev.yavuztas.cap.capsource.feed.FeedSupplier
import dev.yavuztas.cap.capsource.feed.RawFeedData
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.core.net.NetSocket
import org.jetbrains.annotations.NotNull
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import spock.lang.Specification
import spock.util.concurrent.AsyncConditions

import java.nio.charset.StandardCharsets
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import java.util.function.BiConsumer
import java.util.function.Consumer

class RegistrySpec extends Specification {

  static Logger log = LoggerFactory.getLogger(RegistrySpec)

  Vertx vertx
  Vertx clientVertx
  Registry registry
  ScheduledExecutorService writeThread

  def setup() {
    writeThread = Executors.newScheduledThreadPool(1, r -> new Thread(r, "write-thread"))
    clientVertx = Vertx.vertx(new VertxOptions().setEventLoopPoolSize(1))
    vertx = Vertx.vertx(new VertxOptions().setEventLoopPoolSize(8))
    def props = new RegistryProperties('localhost', 7000, 4)
    def mockSupplier = new FeedSupplier() {

      AtomicLong writeIndex = new AtomicLong(1L)

      @Override
      void addConsumer(@NotNull FeedConsumer consumer) {
        // consume periodic
        writeThread.scheduleAtFixedRate({ consumer.consume(this) }, 0, 200, TimeUnit.MILLISECONDS)
      }

      @Override
      long writeIndex() {
        return writeIndex.get()
      }

      @Override
      FeedData get(long readIndex) {
        return null
      }

      @Override
      long forEach(long readIndex, long amount, @NotNull Consumer<FeedData> action) {
        long to = Math.min(writeIndex.get(), amount)
        readIndex = readIndex < 0 ? 0 : readIndex
        (to - readIndex).times {
          action.accept(new RawFeedData("message-${writeIndex.get()}"))
        }
        writeIndex.incrementAndGet()
        return to
      }

      @Override
      long forEachRemaining(long readIndex, @NotNull Consumer<FeedData> action) {
        return 0
      }
    }
    registry = new Registry(props, vertx, [mockSupplier])

    def ac = new AsyncConditions(1)
    def future = registry.init()
    future.onComplete(r -> {
      ac.evaluate { assert r.succeeded() }
    })
    ac.await()
  }

  def cleanup() {
    registry.destroy()
    writeThread.shutdown()
    clientVertx.close()
  }

  NetSocket createClient(BiConsumer<NetSocket, String> onMessage) {
    def ac = new AsyncConditions(1)
    NetSocket socket = null
    def client = clientVertx.createNetClient()
    client.connect(7000, "localhost", r -> {
      if (r.succeeded()) {
        socket = r.result()
        ac.evaluate { assert true }
      } else {
        log.info("Failed to connect: ${r.cause().getMessage()}")
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
    when:
    def ac = new AsyncConditions(10)
    def clients = []
    for (i in 0..<2) {
      clients.add(createClient((s,m) -> {
        log.info("client#${s.localAddress()} got message: ${m}")
        ac.evaluate { assert true }
      }))
    }

    then:
    ac.await(5)

    cleanup:
    clients.each { (it as NetSocket).close() }
  }

  def 'test client message consume order'() {
    given:
    def order = new AtomicInteger(0)
    when:
    def ac = new AsyncConditions(10)
    def client = createClient((s,m) -> {
      log.info("client#${s.localAddress()} got message: ${m}")
      def next = Integer.parseInt(m.split("-")[1])
      if (next - order.getAndSet(next) > 1) {
        throw new IllegalStateException("Client recieved message is not in order")
      }
      ac.evaluate { assert true }
    })

    then:
    ac.await(5)
    noExceptionThrown()

    cleanup:
    client.close()
  }

  def 'test slow client'() {
    when:
    def clientReadIndex = new AtomicLong(0)
    def ac = new AsyncConditions(5)
    def client = createClient((s,m) -> {
      Thread.sleep(1000)
      log.info("client#${s.localAddress()} readIndex: ${clientReadIndex.incrementAndGet()}, message: ${m}")
      ac.evaluate { assert true }
    })

    then:
    ac.await(15)

    cleanup:
    client.close()
  }

}
