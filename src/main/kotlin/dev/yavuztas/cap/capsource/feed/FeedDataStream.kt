package dev.yavuztas.cap.capsource.feed

import io.vertx.core.Handler
import io.vertx.core.buffer.Buffer
import io.vertx.core.streams.ReadStream
import java.util.concurrent.atomic.AtomicLong

class FeedDataStream (private val supplier: FeedSupplier) : ReadStream<Buffer> {

  private var readIndex = AtomicLong(this.supplier.writeIndex() - 1)
  private var demand = AtomicLong(Long.MAX_VALUE)

  private var exceptionHandler: Handler<Throwable>? = null
  private var eventHandler: Handler<Buffer>? = null

  override fun exceptionHandler(handler: Handler<Throwable>?): ReadStream<Buffer> {
    this.exceptionHandler = handler
    return this
  }

  override fun pause(): ReadStream<Buffer> {
    demand.set(0L)
    return this
  }

  override fun resume(): ReadStream<Buffer> {
    return fetch(Long.MAX_VALUE)
  }

  override fun fetch(amount: Long): ReadStream<Buffer> {
    if (demand.addAndGet(amount) < 0L) {
      demand.set(Long.MAX_VALUE)
    }
    doFetch()
    return this
  }

  override fun endHandler(endHandler: Handler<Void>?): ReadStream<Buffer> {
    // no-op, feed data stream is infinite
    return this
  }

  override fun handler(handler: Handler<Buffer>?): ReadStream<Buffer> {
    this.eventHandler = handler
    return this
  }

  fun readIndex(): Long {
    return this.readIndex.get()
  }

  fun supplier(): FeedSupplier {
    return this.supplier
  }

  private fun doFetch() {
    if (demand.get() > 0L) {
      synchronized(this) {
        val writeIndex = this.supplier.forEach(readIndex.get(), demand.get()) {
          this.eventHandler?.handle(it.asReadOnly())
        }
        readIndex.set(writeIndex)
      }
      if (demand.get() != Long.MAX_VALUE) {
        demand.set(0L)
      }
    }
  }

}
