package dev.yavuztas.cap.capsource.registry

import dev.yavuztas.cap.capsource.config.properties.RegistryProperties
import dev.yavuztas.cap.capsource.feed.FeedConsumer
import dev.yavuztas.cap.capsource.feed.FeedSupplier
import io.vertx.core.AbstractVerticle
import io.vertx.core.DeploymentOptions
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.net.NetServer
import io.vertx.core.net.NetServerOptions
import io.vertx.core.net.NetSocket
import mu.KotlinLogging
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
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
) : FeedConsumer {

  private val log = KotlinLogging.logger {}
  private val clients: MutableMap<NetSocket, RegistryClient> = ConcurrentHashMap()

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
      log.debug { "client connected: ${socket.remoteAddress()}" }
      socket.closeHandler { onClose(socket) }
      val client = RegistryClient(socket)
      suppliers.forEach { client.addStream(it) }
      clients[socket] = client
    }

    private fun onClose(socket: NetSocket) {
      log.debug { "client disconnected: ${socket.remoteAddress()}" }
      clients.remove(socket)
    }

  }

  override fun consume(supplier: FeedSupplier) {
    // consume in vertx event pool, each client can consume concurrently
    clients.forEach { it.value.read() }
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
      suppliers.forEach { it.addConsumer(this) }
    }.onFailure { e ->
      log.error("Registry server start-up failed host: ${options.host}, port: ${options.port}", e)
    }
  }

  @PreDestroy
  fun destroy() {
    vertx.close()
  }

}
