package dev.yavuztas.cap.capsource.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "app.vertx")
data class VertxProperties(
  val eventLoopPoolSize: Int
)
