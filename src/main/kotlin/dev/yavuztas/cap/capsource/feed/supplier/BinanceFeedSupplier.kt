package dev.yavuztas.cap.capsource.feed.supplier

import com.binance.connector.client.WebsocketClient
import com.binance.connector.client.impl.WebsocketClientImpl
import com.binance.connector.client.utils.WebSocketCallback
import dev.yavuztas.cap.capsource.feed.FeedConsumer
import dev.yavuztas.cap.capsource.feed.FeedData
import dev.yavuztas.cap.capsource.feed.RawFeedData
import dev.yavuztas.cap.capsource.feed.FeedSupplier
import dev.yavuztas.cap.capsource.util.RingBuffer
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*
import java.util.function.Consumer
import java.util.stream.Collectors
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

  private val buffer: RingBuffer<FeedData> = RingBuffer(bufferSize)

  @PostConstruct
  fun init() {
    // register all symbols given
    val stream = Arrays.stream(symbols)
      .map { s -> "$s@miniTicker" }
      .collect(Collectors.toCollection { ArrayList() })
    client.combineStreams(stream,
      noopCallback, ::onMessage,
      noopCallback, ::onFailure
    )
  }

  private fun onMessage(message: String) {
    this.buffer.add(RawFeedData(message))
    // trigger consumers
    consumers.forEach { it.consume(this)}
    log.info { "supplied: ${message}, write index: ${buffer.writeIndex()}" }
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
    return this.buffer.writeIndex()
  }

  override fun get(readIndex: Long): FeedData? {
    return this.buffer.get(readIndex)
  }

  override fun forEachRemaining(readIndex: Long, action: Consumer<FeedData>): Long {
    return this.buffer.forEachRemaining(readIndex, action)
  }

}
