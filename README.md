# CAP - Crypto API
A PoC for an example financial data stream service. It's based on public cryptocurrency data.  

## cap-source
This project is the upstream part of the tick data stream.

### Overview
* Using [Binance Public API](https://binance-docs.github.io/apidocs) (websocket) to listen to ticker stream of configurable symbols.
* [FeedSupplier](https://github.com/yavuztas/cap-source/blob/master/src/main/kotlin/dev/yavuztas/cap/capsource/feed/FeedSupplier.kt) stores the raw data into a [RingBuffer](https://github.com/yavuztas/cap-source/blob/master/src/main/kotlin/dev/yavuztas/cap/capsource/util/RingBuffer.kt) (a naive implementation by utilizing a fixed size ArrayList)
* Incoming data is consumed and encoded by [FeedConsumer](https://github.com/yavuztas/cap-source/blob/master/src/main/kotlin/dev/yavuztas/cap/capsource/feed/FeedConsumer.kt)s (only naive encoding, doesn't compress) and is redirected to [Registry](https://github.com/yavuztas/cap-source/blob/master/src/main/kotlin/dev/yavuztas/cap/capsource/Registry.kt).
* [Registry](https://github.com/yavuztas/cap-source/blob/master/src/main/kotlin/dev/yavuztas/cap/capsource/Registry.kt) makes a TCP fan-out to the connected TCP clients.
* A unit test demonstrates also client connection can be found in [RegistrySpec](https://github.com/yavuztas/cap-source/blob/master/src/test/groovy/dev/yavuztas/cap/capsource/RegistrySpec.groovy) 

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

source:
  binance:
    buffer-size: 1024
    symbols: btcusdt,ethusdt
```
Via Gradle:
```shell
# profile local enables logging
./gradlew bootRun --args='--spring.profiles.active=local'
```

### 3rd Party Projects/Libraries
Many thanks to these awesome projects:

* [Eclipse Vert.x](https://vertx.io)
* [Kotlin](https://kotlinlang.org)
* [Groovy](https://groovy-lang.org)
* [Spock Framework](https://spockframework.org)

Otherwise, it wouldn't be possible to make this project, at least not in a couple of days :)
