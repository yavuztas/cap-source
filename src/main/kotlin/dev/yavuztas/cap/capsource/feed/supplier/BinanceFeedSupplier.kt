package dev.yavuztas.cap.capsource.feed.supplier

import com.binance.connector.client.WebsocketClient
import com.binance.connector.client.impl.WebsocketClientImpl
import com.binance.connector.client.utils.WebSocketCallback
import dev.yavuztas.cap.capsource.feed.FeedConsumer
import dev.yavuztas.cap.capsource.feed.FeedData
import dev.yavuztas.cap.capsource.feed.FeedSupplier
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.concurrent.atomic.AtomicLong
import java.util.function.Consumer
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

/**
 * @param bufferSize: Must be a power of 2, the modulo calculation depends on that.
 */
@Component
class BinanceFeedSupplier(
  @Value("\${source.binance.symbols}") private val symbols: Array<String>,
  @Value("\${source.binance.buffer-size}") private val bufferSize: Int = 1024
) : FeedSupplier {

  private val log = KotlinLogging.logger {}
  private val noopCallback = WebSocketCallback { _: String? -> }
  private val client: WebsocketClient = WebsocketClientImpl()
  private val consumers: MutableList<FeedConsumer> = ArrayList()

  //TODO will be replaced by a proper RingBuffer
  private val moduloBitMask = (bufferSize - 1).toLong()
  private val buffer: Array<FeedData?> = Array(bufferSize) { null }
  private val writeIndex: AtomicLong = AtomicLong(0)

  @PostConstruct
  fun init() {
    //TODO register all symbols properly
    client.miniTickerStream(
      symbols[0],
      noopCallback, ::onMessage,
      noopCallback, ::onFailure
    )
  }

  private fun onMessage(message: String) {
    val index = relativeIndex(this.writeIndex.getAndIncrement())
    this.buffer[index] = FeedData(message)
    // trigger consumers
    consumers.forEach { it.consume(this)}
    log.info { "add buffer: ${buffer.size}, relative index: $index write index: ${writeIndex.get()}" }
  }

  private fun onFailure(e: String) {
    log.warn { e }
  }

  @PreDestroy
  fun destroy() {
    client.closeAllConnections()
  }

  override fun addConsumer(consumer: FeedConsumer) {
    consumers.add(consumer)
  }

  override fun writeIndex(): Long {
    return writeIndex.get()
  }

  override fun get(readIndex: Long): FeedData? {
    val index = relativeIndex(readIndex)
    return this.buffer[index]
  }

  override fun forEachRemaining(readIndex: Long, action: Consumer<in FeedData>): Long {
    val writeIndex = this.writeIndex.get()
    var start = if (readIndex < 0) 0 else readIndex
    while (start < writeIndex) {
      action.accept(this.buffer[relativeIndex(start)]!!)
      start++
    }
    return writeIndex
  }

  private fun relativeIndex(index: Long): Int {
    return (index and moduloBitMask).toInt()
  }

}
