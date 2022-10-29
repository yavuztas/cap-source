package dev.yavuztas.cap.capsource

import com.binance.connector.client.WebsocketClient
import com.binance.connector.client.impl.WebsocketClientImpl
import com.binance.connector.client.utils.WebSocketCallback
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

@Component
class Source(
  @Value("\${source.binance.secret}")
  var secret: String
) {
  private val log = KotlinLogging.logger {}
  private val noopCallback = WebSocketCallback { _: String? -> }
  private val client: WebsocketClient = WebsocketClientImpl()

  @PostConstruct
  fun afterPropertiesSet() {
    val project = "Kotlin and Spring Boot"
    log.info { "Welcome $project" }
    log.info { "Secret: $secret" }

    client.miniTickerStream(
      "btcusdt",
      noopCallback, ::onMessage,
      noopCallback, ::onFailure
    )
  }

  private fun onMessage(e: String) {
    log.info { "message: $e" }
  }

  private fun onFailure(e: String) {
    log.warn { e }
  }

  @PreDestroy
  fun destroy() {
    client.closeAllConnections()
  }

}
