package dev.yavuztas.cap.capsource

import dev.yavuztas.cap.capsource.config.properties.RegistryProperties
import dev.yavuztas.cap.capsource.feed.FeedConsumer
import dev.yavuztas.cap.capsource.feed.FeedSupplier
import io.netty.buffer.ByteBuf
import io.vertx.core.AbstractVerticle
import io.vertx.core.DeploymentOptions
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.impl.ConcurrentHashSet
import io.vertx.core.net.NetServer
import io.vertx.core.net.NetServerOptions
import io.vertx.core.net.NetSocket
import io.vertx.core.net.impl.NetSocketImpl
import mu.KotlinLogging
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

/**
 * Multiple Registries must share the same Vertx instance.
 * Thus, we can distribute clients if needed.
 */
class Registry(
  private val props: RegistryProperties,
  private val vertx: Vertx,
  private val suppliers: List<FeedSupplier> = ArrayList()
) {

  private val log = KotlinLogging.logger {}
  private val clients: MutableSet<NetSocket> = ConcurrentHashSet()
  private val consumers: ArrayList<FeedDataConsumer> = ArrayList()

  inner class TcpServer(
    private val options: NetServerOptions
  ) : AbstractVerticle() {

    private lateinit var server: NetServer

    override fun start() {
      server = vertx.createNetServer(options)
        .connectHandler(this::onConnect)
      server.listen()
    }

    private fun onConnect(socket: NetSocket) {
      log.info { "client connected: ${socket.remoteAddress()}" }
      socket.closeHandler { onClose(socket) }
      clients.add(socket)
    }

    private fun onClose(socket: NetSocket) {
      log.info { "client disconnected: ${socket.remoteAddress()}" }
      clients.remove(socket)
    }

  }

  /**
   * @param delay: in miliseconds
   */
  inner class FeedDataConsumer(
    private var delay:Long = 1200
  ) : FeedConsumer {

    private var future: ScheduledFuture<*>? = null
    private var readIndex:Long = 0

    override fun consume(readIndex: Long, supplier: FeedSupplier) {
      if (future != null) return // already consuming
      this.readIndex = readIndex
      // submit to internal vertx event pool to consume concurrently
      future = vertx.nettyEventLoopGroup().scheduleAtFixedRate({
        log.info { "<consume> readIndex: ${this.readIndex} <=> writeIndex: ${supplier.writeIndex()}" }
        this.readIndex = supplier.forEachRemaining(this.readIndex) { data ->
          log.info { "<consume> read remaining data: $data" }
          // incoming feed is excoded and published to the registry
          // readonly copy to protect against post-write
          publish(data.asReadOnly())
        }
      }, 0, delay, TimeUnit.MILLISECONDS)

    }

    fun stop() {
      future?.cancel(true)
    }

  }

  @PostConstruct
  fun init(): Future<String> {

    val options = NetServerOptions()
      .setHost(props.host).setPort(props.port)

    return vertx.deployVerticle(
      { TcpServer(options) }, DeploymentOptions().setInstances(props.tcpServerThreadPool)
    ).onSuccess {
      log.info { "Registry server started on ${options.port}" }
      // create and start consuming for each supplier
      suppliers.forEach { supplier ->
        val consumer = FeedDataConsumer(props.feedConsumerDelay)
        consumer.consume(supplier.writeIndex() - 1, supplier)
        consumers.add(consumer)
      }
      log.info { "Initialized feed consumers: ${consumers.size}" }
    }.onFailure { e ->
      log.error("Registry server start-up failed host: ${options.host}, port: ${options.port}", e)
    }
  }

  @PreDestroy
  fun destroy() {
    // TODO do we really need to stop consumers? Or Vertx handles this??
    consumers.forEach { it.stop() }
    vertx.close()
  }

  private fun publish(feed: ByteBuf) {
    clients.stream()
      .forEach { socket ->
        // TODO find out the default queue size
        // write is asynchronious, and it's queued internally via Netty
        writeToSocket(socket, feed)
      }
  }

  private fun writeToSocket(socket: NetSocket, data: ByteBuf) {
    (socket as NetSocketImpl).writeMessage(data)
  }

}
