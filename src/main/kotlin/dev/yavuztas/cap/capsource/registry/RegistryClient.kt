package dev.yavuztas.cap.capsource.registry

import dev.yavuztas.cap.capsource.feed.FeedSupplier
import dev.yavuztas.cap.capsource.feed.FeedDataStream
import io.vertx.core.net.NetSocket
import mu.KotlinLogging

class RegistryClient (private val socket: NetSocket) {

  private val log = KotlinLogging.logger {}
  private val streams: MutableList<FeedDataStream> = ArrayList()

  fun addStream(supplier: FeedSupplier) {
    this.streams.add(FeedDataStream(supplier))
  }

  fun read() {
    streams.forEach {
      it.pipeTo(socket)
      log.debug { "<read> client: ${socket.remoteAddress()}, readIndex: ${it.readIndex()}, writeIndex: ${it.supplier().writeIndex()}" }
    }

  }

}
