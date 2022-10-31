package dev.yavuztas.cap.capsource.feed

import io.netty.buffer.ByteBuf
import io.vertx.core.buffer.Buffer

data class FeedData(private val data: String) {

  object ENCODER {
    fun encode(message: String): Buffer {
      val bytes = message.toByteArray(Charsets.US_ASCII)
      val messageSize = bytes.size
      return Buffer.buffer(2 + messageSize)
        .appendUnsignedShort(messageSize)
        .appendBytes(bytes)
    }
  }

  private val cachedBuffer: Buffer by lazy { ENCODER.encode(data) }

  fun asMutable(): Buffer {
    return this.cachedBuffer
  }

  fun asDuplicate(): ByteBuf {
    return this.cachedBuffer.byteBuf
  }

  fun asReadOnly(): ByteBuf {
    return this.cachedBuffer.byteBuf.asReadOnly()
  }

}


