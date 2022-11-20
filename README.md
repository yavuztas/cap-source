# CAP - Crypto API
A PoC for an example financial data stream service. It's based on public cryptocurrency data.  

## cap-source
This project is the upstream part of the tick data stream.

### Overview
* Using [Binance Public API](https://binance-docs.github.io/apidocs) (websocket) to listen to ticker stream of configurable symbols.
* [FeedSupplier](https://github.com/yavuztas/cap-source/blob/master/src/main/kotlin/dev/yavuztas/cap/capsource/feed/FeedSupplier.kt) stores the raw data into a [RingBuffer](https://github.com/yavuztas/cap-source/blob/master/src/main/kotlin/dev/yavuztas/cap/capsource/util/RingBuffer.kt) (a naive implementation by utilizing a fixed size ArrayList)
* Incoming data is consumed and encoded by [FeedConsumer](https://github.com/yavuztas/cap-source/blob/master/src/main/kotlin/dev/yavuztas/cap/capsource/feed/FeedConsumer.kt)s (only basic encoding (US_ASCII), doesn't compress) and is redirected to [Registry](https://github.com/yavuztas/cap-source/blob/master/src/main/kotlin/dev/yavuztas/cap/capsource/registry/Registry.kt).
* [Registry](https://github.com/yavuztas/cap-source/blob/master/src/main/kotlin/dev/yavuztas/cap/capsource/registry/Registry.kt) makes a TCP fan-out to the connected [RegistryClient](https://github.com/yavuztas/cap-source/blob/master/src/main/kotlin/dev/yavuztas/cap/capsource/registry/RegistryClient.kt)s via [FeedDataStream](https://github.com/yavuztas/cap-source/blob/master/src/main/kotlin/dev/yavuztas/cap/capsource/feed/FeedDataStream.kt) (per client instance) which uses Vertx's [Pipe](https://vertx.io/docs/apidocs/io/vertx/core/streams/Pipe.html) for TCP write backpressure (in case of slow clients).
* A unit test demonstrates also client connection which can be found in [RegistrySpec](https://github.com/yavuztas/cap-source/blob/master/src/test/groovy/dev/yavuztas/cap/capsource/registry/RegistrySpec.groovy) 

### How To Run
For possible configurations, [application.yml](https://github.com/yavuztas/cap-source/blob/master/src/main/resources/application.yml)
```yaml
app:
  vertx:
    event-loop-pool-size: 8
  registry:
    host: localhost
    port: 7000
    tcp_server_thread_pool: 4
    client-read-thread-pool: 8

source:
  binance:
    buffer-size: 1024
    symbols: btcusdt,ethusdt
```
Via Gradle:
```shell
# profile local enables logging
./gradlew bootRun --args='--spring.profiles.active=local --logging.level.dev.yavuztas.cap.capsource=debug'
```

### 3rd Party Projects/Libraries
Many thanks to these awesome projects:

* [Eclipse Vert.x](https://vertx.io)
* [Kotlin](https://kotlinlang.org)
* [Groovy](https://groovy-lang.org)
* [Spock Framework](https://spockframework.org)

Otherwise, it wouldn't be possible to make this project, at least in a reasonable time :)
