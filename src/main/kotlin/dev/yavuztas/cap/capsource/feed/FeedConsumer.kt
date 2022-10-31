package dev.yavuztas.cap.capsource.feed

interface FeedConsumer {

  fun consume(readIndex:Long, supplier: FeedSupplier)

}
