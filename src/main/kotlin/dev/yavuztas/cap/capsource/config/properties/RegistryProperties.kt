package dev.yavuztas.cap.capsource.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "app.registry")
data class RegistryProperties(
  val host: String,
  val port: Int,
  val tcpServerThreadPool: Int,
  val feedConsumerDelay: Long
)
