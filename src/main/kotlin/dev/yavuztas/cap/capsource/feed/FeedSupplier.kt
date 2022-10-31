package dev.yavuztas.cap.capsource.feed

import java.util.function.Consumer

interface FeedSupplier {

  fun writeIndex(): Long

  fun get(readIndex: Long): FeedData?

  /**
   * @return write index at the time of consuming
   */
  fun forEachRemaining(readIndex: Long, action: Consumer<in FeedData>): Long

}
