package dev.yavuztas.cap.capsource.feed

import io.vertx.core.buffer.Buffer

/**
 * Basic FeedData implementation with a naive encoding, does not compress data
 */
class RawFeedData(private val data: ByteArray): FeedData {

  constructor(data: String) : this(data.toByteArray(Charsets.US_ASCII))

  object ENCODER {
    fun encode(bytes: ByteArray): Buffer {
      val messageSize = bytes.size
      return Buffer.buffer(2 + messageSize)
        .appendUnsignedShort(messageSize)
        .appendBytes(bytes)
    }
  }

  private val cachedBuffer: Buffer by lazy { ENCODER.encode(data) }

  override fun asMutable(): Buffer {
    return this.cachedBuffer
  }

  override fun toString(): String {
    return String(data, Charsets.US_ASCII)
  }

}


