# CAP - Crypto API
A PoC for an example financial data stream service. It's based on public cryptocurrency data.  

## cap-source
This project is the upstream part of the tick data stream.

### Overview
* Using [Binance Public API]() (websocket) to listen to ticker stream of configurable symbols.
* [FeedSupplier]() stores the raw data into a [RingBuffer]() (a naive implementation by utilizing a fixed size ArrayList)
* Incoming data is consumed and encoded by [FeedConsumer]()s (only naive encoding, doesn't compress) and is redirected to [Registry]().
* [Registry]() makes a TCP fan-out to the connected TCP clients.
* A unit test demonstrates also client connection can be found in [RegistrySpec]() 

### How To Run
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
