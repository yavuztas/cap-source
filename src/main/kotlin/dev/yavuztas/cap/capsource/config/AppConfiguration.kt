package dev.yavuztas.cap.capsource.config

import dev.yavuztas.cap.capsource.registry.Registry
import dev.yavuztas.cap.capsource.config.properties.RegistryProperties
import dev.yavuztas.cap.capsource.config.properties.VertxProperties
import dev.yavuztas.cap.capsource.feed.FeedSupplier
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class AppConfiguration(
  private val vertxProps: VertxProperties,
  private val registryProps: RegistryProperties
) {

  @Bean
  fun vertx(): Vertx {
    return Vertx.vertx(
      VertxOptions()
        .setEventLoopPoolSize(vertxProps.eventLoopPoolSize)
    )
  }

  @Bean
  fun registry(vertx: Vertx, suppliers: List<FeedSupplier>): Registry {
    return Registry(registryProps, vertx, suppliers)
  }

}
