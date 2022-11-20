package dev.yavuztas.cap.capsource.registry

import dev.yavuztas.cap.capsource.feed.FeedSupplier
import dev.yavuztas.cap.capsource.feed.FeedDataStream
import io.vertx.core.net.NetSocket

class RegistryClient (private val socket: NetSocket) {

  private val streams: MutableList<FeedDataStream> = ArrayList()

  fun addStream(supplier: FeedSupplier) {
    this.streams.add(FeedDataStream(supplier))
  }

  fun read() {
    streams.forEach { it.pipeTo(socket) }
  }

}
