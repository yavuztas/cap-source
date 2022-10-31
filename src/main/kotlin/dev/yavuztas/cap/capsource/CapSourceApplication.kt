package dev.yavuztas.cap.capsource

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class CapSourceApplication

fun main(args: Array<String>) {
  runApplication<CapSourceApplication>(*args)
}
