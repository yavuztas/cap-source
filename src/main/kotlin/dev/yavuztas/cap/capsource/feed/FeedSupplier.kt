package dev.yavuztas.cap.capsource.feed

import java.util.function.Consumer

interface FeedSupplier {

  fun addConsumer(consumer: FeedConsumer)

  fun writeIndex(): Long

  fun get(readIndex: Long): FeedData?

  /**
   * @return read index + amount at the time of consuming
   */
  fun forEach(readIndex: Long, amount: Long, action: Consumer<FeedData>): Long

  /**
   * @return write index at the time of consuming
   */
  fun forEachRemaining(readIndex: Long, action: Consumer<FeedData>): Long

}
