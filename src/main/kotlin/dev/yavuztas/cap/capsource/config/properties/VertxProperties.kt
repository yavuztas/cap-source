package dev.yavuztas.cap.capsource.config.properties

import io.vertx.core.VertxOptions
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "app.vertx")
data class VertxProperties(
  var eventLoopPoolSize: Int = VertxOptions.DEFAULT_EVENT_LOOP_POOL_SIZE
)
