package dev.yavuztas.cap.capsource.util

import java.util.concurrent.atomic.AtomicLong
import java.util.function.Consumer

private fun isPowerOfTwo(n: Int): Boolean {
  return n != 0 && n and n - 1 == 0
}

/**
 * @param bufferSize: Must be a power of 2, the modulo calculation depends on that.
 */
class RingBuffer<T>(bufferSize: Int) {
  init {
    if (!isPowerOfTwo(bufferSize))
      throw IllegalArgumentException("bufferSize must be a power of 2")
  }

  private val buffer = mutableListOf<T?>().apply {
    for (index in 0 until bufferSize) {
      add(null)
    }
  }
  private val moduloBitMask = (bufferSize - 1).toLong()
  private val writeIndex: AtomicLong = AtomicLong(0)

  fun size(): Int {
    return this.buffer.size
  }

  fun add(e: T) {
    val index = relativeIndex(this.writeIndex.getAndIncrement())
    this.buffer[index] = e
  }

  fun writeIndex(): Long {
    return writeIndex.get()
  }

  fun remaining(readIndex: Long): Long {
    return writeIndex.get() - readIndex
  }

  fun hasRemaining(readIndex: Long): Boolean {
    return writeIndex.get() > readIndex
  }

  fun get(readIndex: Long): T? {
    val index = relativeIndex(readIndex)
    return this.buffer[index]
  }

  fun forEachRemaining(readIndex: Long, action: Consumer<T>): Long {
    val writeIndex = this.writeIndex.get()
    var start = if (readIndex < 0) 0 else readIndex
    while (start < writeIndex) {
      action.accept(this.buffer[relativeIndex(start)]!!)
      start++
    }
    return writeIndex
  }

  private fun relativeIndex(index: Long): Int {
    // this is faster than % operator but limited to the size of power of 2
    return (index and moduloBitMask).toInt()
  }


}
