package dev.yavuztas.cap.capsource.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "app.registry")
data class RegistryProperties(
  var host: String = "localhost",
  var port: Int = 7000,
  var tcpServerThreadPool: Int = 4,
  var feedConsumerDelay: Long = 1200L
)
