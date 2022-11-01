package dev.yavuztas.cap.capsource.feed

interface FeedConsumer {

  /**
   * Implementors should implement this method in a non-blocking way
   * Otherwise, it may block the supplier.
   */
  fun consume(supplier: FeedSupplier)

}
